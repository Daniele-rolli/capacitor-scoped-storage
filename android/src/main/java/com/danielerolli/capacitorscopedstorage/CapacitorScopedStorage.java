package com.danielerolli.capacitorscopedstorage;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class CapacitorScopedStorage {
    private static final String TAG = "CapScopedStorage";
    private final Context context;

    public CapacitorScopedStorage(Context ctx) {
        this.context = ctx.getApplicationContext();
    }

    // ---------- File Ops ----------
    public void writeFile(PluginCall call) {
        try {
            DocumentFile f = ensureFile(call);
            String data = nonNull(call.getString("data"), "data missing");
            String enc  = call.getString("encoding", "utf8");

            byte[] bytes = "base64".equalsIgnoreCase(enc)
                    ? Base64.decode(data, Base64.DEFAULT)
                    : data.getBytes(StandardCharsets.UTF_8);

            try (OutputStream os = context.getContentResolver().openOutputStream(f.getUri(), "w")) {
                if (os == null) throw new Exception("Failed to open output stream");
                os.write(bytes);
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("writeFile failed: " + e.getMessage());
        }
    }

    public void appendFile(PluginCall call) {
        try {
            DocumentFile f = ensureFile(call);
            String data = nonNull(call.getString("data"), "data missing");
            String enc  = call.getString("encoding", "utf8");

            byte[] bytes = "base64".equalsIgnoreCase(enc)
                    ? Base64.decode(data, Base64.DEFAULT)
                    : data.getBytes(StandardCharsets.UTF_8);

            try (OutputStream os = context.getContentResolver().openOutputStream(f.getUri(), "wa")) {
                if (os == null) throw new Exception("Failed to open output stream");
                os.write(bytes);
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("appendFile failed: " + e.getMessage());
        }
    }

    public void readFile(PluginCall call) {
        try {
            DocumentFile f = resolveFile(call);
            if (f == null || !f.isFile()) {
                call.reject("File not found");
                return;
            }

            try (InputStream is = context.getContentResolver().openInputStream(f.getUri())) {
                if (is == null) throw new Exception("Failed to open input stream");
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) != -1) buffer.write(buf, 0, n);

                String enc = call.getString("encoding", "utf8");
                JSObject ret = new JSObject();
                if ("base64".equalsIgnoreCase(enc)) {
                    ret.put("data", Base64.encodeToString(buffer.toByteArray(), Base64.NO_WRAP));
                } else {
                    ret.put("data", new String(buffer.toByteArray(), StandardCharsets.UTF_8));
                }
                call.resolve(ret);
            }
        } catch (Exception e) {
            call.reject("readFile failed: " + e.getMessage());
        }
    }

    // ---------- Dir Ops ----------
    public void mkdir(PluginCall call) {
        try {
            String[] parts = splitPath(getPath(call));
            DocumentFile cur = requireBase(call);

            for (String p : parts) {
                DocumentFile next = cur.findFile(p);
                if (next == null) {
                    next = cur.createDirectory(p);
                    if (next == null) throw new Exception("Failed to create directory: " + p);
                }
                cur = next;
            }
            call.resolve();
        } catch (Exception e) {
            call.reject("mkdir failed: " + e.getMessage());
        }
    }

    public void rmdir(PluginCall call) {
        try {
            DocumentFile dir = resolveFile(call);
            if (dir == null || !dir.isDirectory()) {
                call.reject("Not a directory");
                return;
            }
            boolean recursive = call.getBoolean("recursive", false);
            if (!recursive && dir.listFiles().length > 0) {
                call.reject("Directory not empty");
                return;
            }
            deleteRecursively(dir);
            call.resolve();
        } catch (Exception e) {
            call.reject("rmdir failed: " + e.getMessage());
        }
    }

    public void readdir(PluginCall call) {
        try {
            DocumentFile dir = call.hasOption("path") ? resolveFile(call) : requireBase(call);
            if (dir == null || !dir.isDirectory()) {
                call.reject("Not a directory");
                return;
            }
            JSArray arr = new JSArray();
            for (DocumentFile f : dir.listFiles()) {
                JSObject entry = new JSObject();
                entry.put("name", f.getName());
                entry.put("isDir", f.isDirectory());
                entry.put("size", f.isFile() ? f.length() : null);
                entry.put("mtime", f.lastModified() > 0 ? (f.lastModified() / 1000.0) : null);
                arr.put(entry);
            }
            call.resolve(new JSObject().put("entries", arr));
        } catch (Exception e) {
            call.reject("readdir failed: " + e.getMessage());
        }
    }

    public void stat(PluginCall call) {
        try {
            DocumentFile f = resolveFile(call);
            if (f == null) {
                call.reject("Not found");
                return;
            }
            JSObject ret = new JSObject();
            ret.put("uri", f.getUri().toString());
            ret.put("size", f.isFile() ? f.length() : null);
            ret.put("mtime", f.lastModified() > 0 ? (f.lastModified() / 1000.0) : null);
            ret.put("type", f.isDirectory() ? "directory" : (f.isFile() ? "file" : "unknown"));
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("stat failed: " + e.getMessage());
        }
    }

    public void exists(PluginCall call) {
        DocumentFile f = resolveFile(call);
        boolean ok = f != null;
        call.resolve(new JSObject()
            .put("exists", ok)
            .put("isDirectory", ok && f.isDirectory()));
    }

    public void deleteFile(PluginCall call) {
        try {
            DocumentFile f = resolveFile(call);
            if (f != null && !f.delete()) throw new Exception("Delete returned false");
            call.resolve();
        } catch (Exception e) {
            call.reject("deleteFile failed: " + e.getMessage());
        }
    }

    public void move(PluginCall call) { moveOrCopy(call, true); }
    public void copy(PluginCall call) { moveOrCopy(call, false); }

    private void moveOrCopy(PluginCall call, boolean isMove) {
        try {
            String from = nonNull(call.getString("from"), "'from' missing");
            String to   = nonNull(call.getString("to"),   "'to' missing");
            boolean overwrite = call.getBoolean("overwrite", false);

            DocumentFile base = requireBase(call);
            DocumentFile src  = findFile(base, from);
            if (src == null) throw new Exception("Source not found: " + from);

            String[] parts = splitPath(to);
            String fileName = parts[parts.length - 1];

            DocumentFile parent = base;
            for (int i = 0; i < parts.length - 1; i++) {
                DocumentFile next = parent.findFile(parts[i]);
                if (next == null) {
                    next = parent.createDirectory(parts[i]);
                    if (next == null) throw new Exception("Failed to create directory: " + parts[i]);
                }
                parent = next;
            }

            DocumentFile dst = parent.findFile(fileName);
            if (dst != null) {
                if (!overwrite) throw new Exception("Destination exists and overwrite=false");
                if (!dst.delete()) throw new Exception("Failed to delete existing destination");
                dst = null;
            }
            if (dst == null) {
                dst = parent.createFile("application/octet-stream", fileName);
                if (dst == null) throw new Exception("Failed to create destination file");
            }

            try (InputStream is = context.getContentResolver().openInputStream(src.getUri());
                 OutputStream os = context.getContentResolver().openOutputStream(dst.getUri(), "w")) {
                if (is == null || os == null) throw new Exception("Failed to open streams");
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) != -1) os.write(buf, 0, n);
            }

            if (isMove && !src.delete()) {
                Log.w(TAG, "Move: failed to delete source (vendor quirk?) " + src.getUri());
            }
            call.resolve();
        } catch (Exception e) {
            call.reject((isMove ? "move" : "copy") + " failed: " + e.getMessage());
        }
    }

    public void getUriForPath(PluginCall call) {
        try {
            DocumentFile f = resolveFile(call);
            call.resolve(new JSObject().put("uri", f != null ? f.getUri().toString() : null));
        } catch (Exception e) {
            call.reject("getUriForPath failed: " + e.getMessage());
        }
    }

    // ---------- Helpers ----------
    private String getFolderId(PluginCall call) throws Exception {
        JSObject folder = call.getObject("folder");
        if (folder == null) throw new Exception("'folder' missing");
        String id = folder.getString("id");
        if (id == null || id.isEmpty()) throw new Exception("'folder.id' missing");
        return id;
    }

    private String getPath(PluginCall call) throws Exception {
        String p = call.getString("path");
        if (p == null || p.trim().isEmpty()) throw new Exception("'path' missing");
        return p;
    }

    private DocumentFile requireBase(PluginCall call) throws Exception {
        Uri tree = Uri.parse(getFolderId(call));
        DocumentFile base = DocumentFile.fromTreeUri(context, tree);
        if (base == null) throw new Exception("Invalid folder URI");
        return base;
    }

    private DocumentFile resolveFile(PluginCall call) {
        try {
            String path = getPath(call);
            DocumentFile cur = requireBase(call);
            for (String seg : splitPath(path)) {
                DocumentFile next = cur.findFile(seg);
                if (next == null) return null;
                cur = next;
            }
            return cur;
        } catch (Exception e) {
            return null;
        }
    }

    private DocumentFile ensureFile(PluginCall call) throws Exception {
        String path = getPath(call);
        String mime = call.getString("mimeType");
        if (mime == null || mime.trim().isEmpty()) mime = "text/plain";

        String[] parts = splitPath(path);
        DocumentFile cur = requireBase(call);
        for (int i = 0; i < parts.length - 1; i++) {
            DocumentFile next = cur.findFile(parts[i]);
            if (next == null) {
                next = cur.createDirectory(parts[i]);
                if (next == null) throw new Exception("Failed to create directory: " + parts[i]);
            }
            cur = next;
        }

        String fileName = parts[parts.length - 1];
        DocumentFile f = cur.findFile(fileName);
        if (f == null) {
            f = cur.createFile(mime, fileName);
            if (f == null) throw new Exception("Failed to create file: " + fileName);
        }
        return f;
    }

    private void deleteRecursively(DocumentFile entry) {
        if (entry.isDirectory()) {
            for (DocumentFile c : entry.listFiles()) deleteRecursively(c);
        }
        // Best effort
        if (!entry.delete()) {
            Log.w(TAG, "Delete returned false for " + entry.getUri());
        }
    }

    private DocumentFile findFile(DocumentFile root, String relPath) {
        if (root == null) return null;
        DocumentFile cur = root;
        for (String p : splitPath(relPath)) {
            DocumentFile next = cur.findFile(p);
            if (next == null) return null;
            cur = next;
        }
        return cur;
    }

    private static String nonNull(String v, String msg) throws Exception {
        if (v == null) throw new Exception(msg);
        return v;
    }

    private static String[] splitPath(String p) {
        return p.replace("\\", "/").split("/");
    }
}
