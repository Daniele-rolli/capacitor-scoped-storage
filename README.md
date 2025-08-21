# 🗂️ Capacitor Scoped Storage

A Capacitor plugin for **secure, user-approved file and folder access** on **iOS** and **Android**, built on top of platform-native **Security Scoped Bookmarks** (iOS) and **Storage Access Framework (SAF)** (Android).

<p align="center">
  <strong>
    <code>@daniele-rolli/capacitor-scoped-storage</code>
  </strong>
</p>

This plugin lets your app request a folder from the user and then perform safe file operations (read, write, append, delete, move, copy, stat, etc.) within that scope, with **persistent access across app restarts**.

👉 For a real-world setup, check out the [example-app](./example-app) with a full implementation.

## 📑 Table of Contents

* [✨ Features](#-features)
* [📦 Installation](#-installation)
* [🛠 Usage](#-usage)

  * [Pick a Folder](#pick-a-folder)
  * [Write & Read a File](#write--read-a-file)
  * [List Directory Contents](#list-directory-contents)
  * [Move & Copy](#move--copy)
* [📚 API Reference](#-api-reference)
* [📝 API Types](#-api-types)

  * [Folder & Folder Selection](#folder--folder-selection)
  * [File Operations](#file-operations)
  * [Directory Operations](#directory-operations)
  * [File Metadata & Existence](#file-metadata--existence)
  * [File Management (Delete, Move, Copy)](#file-management-delete-move-copy)
  * [Path Utility](#path-utility)
  * [iOS Setup](#ios)
* [📄 License](#-license)

## ✨ Features

* 🔒 **Scoped access**: Operates only within the folder the user selects
* 📂 Cross-platform:

  * **iOS** → Security-scoped bookmarks
  * **Android** → Storage Access Framework (SAF)
* 📑 File operations: `readFile`, `writeFile`, `appendFile`, `deleteFile`
* 📦 Directory management: `mkdir`, `rmdir`, `readdir`
* 🔍 Metadata utilities: `stat`, `exists`
* 📎 Path management: `move`, `copy`, `getUriForPath`
* Persistent folder permissions (reusable bookmarks/tree URIs)
* UTF-8 and Base64 encoding support

## 📦 Installation

```bash
npm install @daniele-rolli/capacitor-scoped-storage
npx cap sync
```

**Requirements:**

* iOS deployment target: **14.0+**
* Android: **API 21+**

## 🛠 Usage

### Pick a Folder

```ts
const { folder } = await ScopedStorage.pickFolder();
// { folder: { id: "...", name: "Documents" } }
```

### Write & Read a File

```ts
await ScopedStorage.writeFile({
  folder,
  path: 'notes/today.txt',
  data: 'Hello world!',
});

const res = await ScopedStorage.readFile({
  folder,
  path: 'notes/today.txt',
});

console.log(res.data); // "Hello world!"
```

### List Directory Contents

```ts
const { entries } = await ScopedStorage.readdir({ folder, path: 'notes' });

console.log(entries);
// [{ name: "today.txt", isDir: false, size: 123, mtime: 1710000000 }]
```

### Move & Copy

```ts
await ScopedStorage.move({
  folder,
  from: 'notes/today.txt',
  to: 'archive/today.txt',
  overwrite: true,
});

await ScopedStorage.copy({
  folder,
  from: 'archive/today.txt',
  to: 'backup/today.txt',
});
```

---

## 📚 API Reference

### Plugin Methods

* `pickFolder(): Promise<PickFolderResult>`
* `writeFile(options: WriteOptions): Promise<void>`
* `appendFile(options: AppendOptions): Promise<void>`
* `readFile(options: ReadOptions): Promise<{ data: string }>`
* `mkdir(options: MkdirOptions): Promise<void>`
* `rmdir(options: RmdirOptions): Promise<void>`
* `readdir(options: ReaddirOptions): Promise<ReaddirResult>`
* `stat(options: StatOptions): Promise<StatResult>`
* `exists(options: ExistsOptions): Promise<ExistsResult>`
* `deleteFile(options: DeleteOptions): Promise<void>`
* `move(options: MoveCopyOptions): Promise<void>`
* `copy(options: MoveCopyOptions): Promise<void>`
* `getUriForPath(options: UriForPathOptions): Promise<UriForPathResult>`

---

## 📝 API Types

### Folder & Folder Selection

```ts
export interface FolderRef {
  /** Android: tree URI; iOS: base64 security-scoped bookmark */
  id: string;
  name?: string;
}

export interface PickFolderResult {
  folder: FolderRef;
}
```

### File Operations

```ts
export interface WriteOptions {
  folder: FolderRef;
  path: string; // relative to folder
  data: string; // utf8 or base64
  encoding?: 'utf8' | 'base64';
}

export interface AppendOptions extends WriteOptions {}

export interface ReadOptions {
  folder: FolderRef;
  path: string;
  encoding?: 'utf8' | 'base64';
}
```

### Directory Operations

```ts
export interface MkdirOptions {
  folder: FolderRef;
  path: string;
  recursive?: boolean;
}

export interface RmdirOptions {
  folder: FolderRef;
  path: string;
  recursive?: boolean;
}

export interface ReaddirOptions {
  folder: FolderRef;
  path?: string;
}

export interface ReaddirResult {
  entries: {
    name: string;
    isDir: boolean;
    size?: number | null;
    mtime?: number | null;
  }[];
}
```

### File Metadata & Existence

```ts
export interface StatOptions {
  folder: FolderRef;
  path: string;
}

export interface StatResult {
  uri?: string;
  size?: number | null;
  mtime?: number | null;
  type: 'file' | 'directory' | 'unknown';
}

export interface ExistsOptions {
  folder: FolderRef;
  path: string;
}

export interface ExistsResult {
  exists: boolean;
  isDirectory: boolean;
}
```

### File Management (Delete, Move, Copy)

```ts
export interface DeleteOptions {
  folder: FolderRef;
  path: string;
}

export interface MoveCopyOptions {
  folder: FolderRef;
  from: string;
  to: string;
  overwrite?: boolean;
}
```

### Path Utility

```ts
export interface UriForPathOptions {
  folder: FolderRef;
  path: string;
}

export interface UriForPathResult {
  uri: string | null;
}
```

### iOS

Add the following to your **Info.plist**:

```xml
<key>LSSupportsOpeningDocumentsInPlace</key>
<true/>
<key>UISupportsDocumentBrowser</key>
<false/>
```

## 📄 License

[MIT](./LICENSE)
**Author:** Daniele Rolli

