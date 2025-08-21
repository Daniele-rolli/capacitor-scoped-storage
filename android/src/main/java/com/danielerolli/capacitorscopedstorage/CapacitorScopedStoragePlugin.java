package com.danielerolli.capacitorscopedstorage;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.result.ActivityResult;           // ✅ ADD THIS
import androidx.documentfile.provider.DocumentFile;       // (used for nicer folder names)

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "ScopedStorage")
public class CapacitorScopedStoragePlugin extends Plugin {
    private CapacitorScopedStorage impl;

    @Override
    public void load() {
        // ✅ match the one-arg constructor from the cleaned implementation
        impl = new CapacitorScopedStorage(getContext());
    }

    @PluginMethod
    public void pickFolder(PluginCall call) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        );

        // ✅ Capacitor 7 pattern: keeps the call alive and routes to @ActivityCallback
        startActivityForResult(call, intent, "onFolderPicked");
    }

    @SuppressLint("WrongConstant")
    @ActivityCallback
    private void onFolderPicked(PluginCall call, ActivityResult result) {
        if (result == null || result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
            call.reject("User cancelled");
            return;
        }

        Intent data = result.getData();
        Uri uri = data.getData();
        if (uri == null) {
            call.reject("No folder URI returned");
            return;
        }

        try {
            getContext().getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (SecurityException ignored) {
            // Some OEMs may throw; transient grant still works for the session.
        }

        // Friendlier folder name if possible
        DocumentFile doc = DocumentFile.fromTreeUri(getContext(), uri);
        String name = (doc != null && doc.getName() != null) ? doc.getName() : uri.getLastPathSegment();

        JSObject folder = new JSObject();
        folder.put("id", uri.toString());
        folder.put("name", name);

        call.resolve(new JSObject().put("folder", folder));
    }

    // Forward other plugin methods to your implementation class
    @PluginMethod public void writeFile(PluginCall call) { impl.writeFile(call); }
    @PluginMethod public void appendFile(PluginCall call) { impl.appendFile(call); }
    @PluginMethod public void readFile(PluginCall call) { impl.readFile(call); }
    @PluginMethod public void mkdir(PluginCall call) { impl.mkdir(call); }
    @PluginMethod public void rmdir(PluginCall call) { impl.rmdir(call); }
    @PluginMethod public void readdir(PluginCall call) { impl.readdir(call); }
    @PluginMethod public void stat(PluginCall call) { impl.stat(call); }
    @PluginMethod public void exists(PluginCall call) { impl.exists(call); }
    @PluginMethod public void deleteFile(PluginCall call) { impl.deleteFile(call); }
    @PluginMethod public void move(PluginCall call) { impl.move(call); }
    @PluginMethod public void copy(PluginCall call) { impl.copy(call); }
    @PluginMethod public void getUriForPath(PluginCall call) { impl.getUriForPath(call); }
}
