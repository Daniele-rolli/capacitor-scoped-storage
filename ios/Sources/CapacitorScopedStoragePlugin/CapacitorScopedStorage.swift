
import Foundation
import Capacitor
import UniformTypeIdentifiers

@objc(CapacitorScopedStorage)
public class CapacitorScopedStorage: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "ScopedStorage"
    public let jsName = "ScopedStorage"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "pickFolder", returnType: CAPPluginReturnPromise),

        CAPPluginMethod(name: "writeFile", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "appendFile", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "readFile", returnType: CAPPluginReturnPromise),

        CAPPluginMethod(name: "mkdir", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "rmdir", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "readdir", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "stat", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "exists", returnType: CAPPluginReturnPromise),

        CAPPluginMethod(name: "deleteFile", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "move", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "copy", returnType: CAPPluginReturnPromise),

        CAPPluginMethod(name: "getUriForPath", returnType: CAPPluginReturnPromise),
    ]

    // MARK: - Helper: resolve bookmark → folder URL with scoped access
    private func withFolderURL(_ call: CAPPluginCall, _ block: (URL) throws -> Void) {
        guard let folder = call.getObject("folder"),
              let id = folder["id"] as? String,
              let bookmarkData = Data(base64Encoded: id)
        else {
            call.reject("Missing or invalid folder.id (bookmark)")
            return
        }
        var stale = false
        do {
            let url = try URL(resolvingBookmarkData: bookmarkData,
                              options: [],
                              relativeTo: nil,
                              bookmarkDataIsStale: &stale)
            guard url.startAccessingSecurityScopedResource() else {
                call.reject("Failed to access security scoped resource")
                return
            }
            defer { url.stopAccessingSecurityScopedResource() }
            try block(url)
        } catch {
            call.reject("Bookmark resolution failed: \(error.localizedDescription)")
        }
    }

    // MARK: - pickFolder
    @objc func pickFolder(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let vc = UIDocumentPickerViewController(forOpeningContentTypes: [.folder], asCopy: false)
            vc.allowsMultipleSelection = false
            vc.delegate = self
            self.bridge?.viewController?.present(vc, animated: true)
            self.savedCall = call
        }
    }

    private var savedCall: CAPPluginCall?

    private func returnPickedFolder(url: URL) {
        do {
            let bookmark = try url.bookmarkData(options: [],
                                                includingResourceValuesForKeys: nil,
                                                relativeTo: nil)
            let b64 = bookmark.base64EncodedString()
            let name = (try? url.resourceValues(forKeys: [.nameKey]).name) ?? url.lastPathComponent
            savedCall?.resolve([
                "folder": ["id": b64, "name": name]
            ])
        } catch {
            savedCall?.reject("Failed to create bookmark: \(error.localizedDescription)")
        }
        savedCall = nil
    }

    // MARK: - read / write / append
    @objc func writeFile(_ call: CAPPluginCall) {
        withFolderURL(call) { folderURL in
            guard let path = call.getString("path"),
                  let dataStr = call.getString("data") else { throw CapacitorError.invalidArgument }
            let encoding = call.getString("encoding") ?? "utf8"
            let data: Data = (encoding == "base64")
                ? Data(base64Encoded: dataStr) ?? Data()
                : Data(dataStr.utf8)

            let target = folderURL.appendingPathComponents(path.split(separator: "/").map(String.init))
            try FileManager.default.createDirectory(at: target.deletingLastPathComponent(), withIntermediateDirectories: true)
            try self.coordinatedWrite(data: data, to: target, replace: true)
            call.resolve()
        }
    }

    @objc func appendFile(_ call: CAPPluginCall) {
        withFolderURL(call) { folderURL in
            guard let path = call.getString("path"),
                  let dataStr = call.getString("data") else { throw CapacitorError.invalidArgument }
            let encoding = call.getString("encoding") ?? "utf8"
            let data: Data = (encoding == "base64")
                ? Data(base64Encoded: dataStr) ?? Data()
                : Data(dataStr.utf8)

            let target = folderURL.appendingPathComponents(path.split(separator: "/").map(String.init))
            try FileManager.default.createDirectory(at: target.deletingLastPathComponent(), withIntermediateDirectories: true)
            try self.coordinatedAppend(data: data, to: target)
            call.resolve()
        }
    }

    @objc func readFile(_ call: CAPPluginCall) {
        withFolderURL(call) { folderURL in
            guard let path = call.getString("path") else { throw CapacitorError.invalidArgument }
            let encoding = call.getString("encoding") ?? "utf8"
            let target = folderURL.appendingPathComponents(path.split(separator: "/").map(String.init))
            let data = try Data(contentsOf: target)

            if encoding == "base64" {
                call.resolve(["data": data.base64EncodedString()])
            } else {
                call.resolve(["data": String(decoding: data, as: UTF8.self)])
            }
        }
    }

    // MARK: - mkdir / rmdir / readdir / stat / exists
    @objc func mkdir(_ call: CAPPluginCall) {
        withFolderURL(call) { folderURL in
            guard let path = call.getString("path") else { throw CapacitorError.invalidArgument }
            let rec = call.getBool("recursive") ?? true
            let dir = folderURL.appendingPathComponents(path.split(separator: "/").map(String.init), isDirectory: true)
            try FileManager.default.createDirectory(at: dir, withIntermediateDirectories: rec)
            call.resolve()
        }
    }

    @objc func rmdir(_ call: CAPPluginCall) {
        withFolderURL(call) { folderURL in
            guard let path = call.getString("path") else { throw CapacitorError.invalidArgument }
            let recursive = call.getBool("recursive") ?? false
            let dir = folderURL.appendingPathComponents(path.split(separator: "/").map(String.init), isDirectory: true)
            if recursive {
                try FileManager.default.removeItem(at: dir)
            } else {
                let contents = try FileManager.default.contentsOfDirectory(atPath: dir.path)
                guard contents.isEmpty else { throw NSError(domain: "ScopedStorage", code: 1, userInfo: [NSLocalizedDescriptionKey: "Directory not empty"]) }
                try FileManager.default.removeItem(at: dir)
            }
            call.resolve()
        }
    }

    @objc func readdir(_ call: CAPPluginCall) {
        withFolderURL(call) { folderURL in
            let rel = call.getString("path") ?? ""
            let dir = rel.isEmpty ? folderURL : folderURL.appendingPathComponents(rel.split(separator: "/").map(String.init), isDirectory: true)
            var result: [[String: Any]] = []
            let keys: [URLResourceKey] = [.isDirectoryKey, .fileSizeKey, .contentModificationDateKey, .nameKey]
            let urls = try FileManager.default.contentsOfDirectory(at: dir, includingPropertiesForKeys: keys, options: [])
            for u in urls {
                let vals = try u.resourceValues(forKeys: Set(keys))
                result.append([
                    "name": vals.name ?? u.lastPathComponent,
                    "isDir": vals.isDirectory ?? false,
                    "size": vals.fileSize as Any? ?? NSNull(),
                    "mtime": (vals.contentModificationDate?.timeIntervalSince1970 as Any?) ?? NSNull()
                ].compactMapValues { $0 })
            }
            call.resolve(["entries": result])
        }
    }

    @objc func stat(_ call: CAPPluginCall) {
        withFolderURL(call) { folderURL in
            guard let path = call.getString("path") else { throw CapacitorError.invalidArgument }
            let u = folderURL.appendingPathComponents(path.split(separator: "/").map(String.init))
            let vals = try u.resourceValues(forKeys: [.isDirectoryKey, .fileSizeKey, .contentModificationDateKey])
            let type: String = (vals.isDirectory ?? false) ? "directory" : FileManager.default.fileExists(atPath: u.path) ? "file" : "unknown"
            call.resolve([
                "uri": u.absoluteString,
                "size": (vals.fileSize as Any?) ?? NSNull(),
                "mtime": (vals.contentModificationDate?.timeIntervalSince1970 as Any?) ?? NSNull(),
                "type": type
            ])
        }
    }

    @objc func exists(_ call: CAPPluginCall) {
        withFolderURL(call) { folderURL in
            guard let path = call.getString("path") else { throw CapacitorError.invalidArgument }
            let u = folderURL.appendingPathComponents(path.split(separator: "/").map(String.init))
            var isDir: ObjCBool = false
            let ok = FileManager.default.fileExists(atPath: u.path, isDirectory: &isDir)
            call.resolve(["exists": ok, "isDirectory": ok ? isDir.boolValue : false])
        }
    }

    // MARK: - delete / move / copy
    @objc func deleteFile(_ call: CAPPluginCall) {
        withFolderURL(call) { folderURL in
            guard let path = call.getString("path") else { throw CapacitorError.invalidArgument }
            let u = folderURL.appendingPathComponents(path.split(separator: "/").map(String.init))
            try FileManager.default.removeItem(at: u)
            call.resolve()
        }
    }

    @objc func move(_ call: CAPPluginCall) {
        withFolderURL(call) { folderURL in
            guard let from = call.getString("from"), let to = call.getString("to") else { throw CapacitorError.invalidArgument }
            let src = folderURL.appendingPathComponents(from.split(separator: "/").map(String.init))
            let dst = folderURL.appendingPathComponents(to.split(separator: "/").map(String.init))
            if call.getBool("overwrite") == true, FileManager.default.fileExists(atPath: dst.path) {
                try FileManager.default.removeItem(at: dst)
            } else {
                try FileManager.default.createDirectory(at: dst.deletingLastPathComponent(), withIntermediateDirectories: true)
            }
            try FileManager.default.moveItem(at: src, to: dst)
            call.resolve()
        }
    }

    @objc func copy(_ call: CAPPluginCall) {
        withFolderURL(call) { folderURL in
            guard let from = call.getString("from"), let to = call.getString("to") else { throw CapacitorError.invalidArgument }
            let src = folderURL.appendingPathComponents(from.split(separator: "/").map(String.init))
            let dst = folderURL.appendingPathComponents(to.split(separator: "/").map(String.init))
            if call.getBool("overwrite") == true, FileManager.default.fileExists(atPath: dst.path) {
                try FileManager.default.removeItem(at: dst)
            } else {
                try FileManager.default.createDirectory(at: dst.deletingLastPathComponent(), withIntermediateDirectories: true)
            }
            try FileManager.default.copyItem(at: src, to: dst)
            call.resolve()
        }
    }

    // MARK: - getUriForPath
    @objc func getUriForPath(_ call: CAPPluginCall) {
        withFolderURL(call) { folderURL in
            guard let path = call.getString("path") else { throw CapacitorError.invalidArgument }
            let u = folderURL.appendingPathComponents(path.split(separator: "/").map(String.init))
            call.resolve(["uri": u.absoluteString])
        }
    }

    // MARK: - File coordination helpers (important for cloud providers)
    private func coordinatedWrite(data: Data, to url: URL, replace: Bool) throws {
        var coordError: NSError?
        var writeError: Error?
        let coordinator = NSFileCoordinator(filePresenter: nil)
        coordinator.coordinate(writingItemAt: url,
                            options: replace ? .forReplacing : .forMerging,
                            error: &coordError) { dest in
            do {
                try data.write(to: dest, options: .atomic)
            } catch {
                writeError = error
            }
        }
        if let e = coordError { throw e }
        if let e = writeError { throw e }
    }

    private func coordinatedAppend(data: Data, to url: URL) throws {
        var coordError: NSError?
        var appendError: Error?
        let coordinator = NSFileCoordinator(filePresenter: nil)
        coordinator.coordinate(writingItemAt: url,
                            options: .forMerging,
                            error: &coordError) { dest in
            do {
                if FileManager.default.fileExists(atPath: dest.path) {
                    let handle = try FileHandle(forWritingTo: dest)
                    defer { try? handle.close() }
                    try handle.seekToEnd()
                    try handle.write(contentsOf: data)
                } else {
                    try data.write(to: dest, options: .atomic)
                }
            } catch {
                appendError = error
            }
        }
        if let e = coordError { throw e }
        if let e = appendError { throw e }
    }
}

// MARK: - Document Picker Delegate
extension CapacitorScopedStorage: UIDocumentPickerDelegate {
    public func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
        guard let u = urls.first else {
            savedCall?.reject("No folder selected")
            savedCall = nil
            return
        }
        returnPickedFolder(url: u)
    }

    public func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
        savedCall?.reject("User cancelled")
        savedCall = nil
    }
}

// MARK: - Tiny conveniences
fileprivate enum CapacitorError: Error { case invalidArgument }

fileprivate extension URL {
    func appendingPathComponents(_ components: [String], isDirectory: Bool = false) -> URL {
        var u = self
        for c in components { u.appendPathComponent(c) }
        return u
    }
}
