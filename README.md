# 🗂️ Capacitor Scoped Storage

A Capacitor plugin for **secure, user-approved file and folder access** on **iOS** and **Android**, built on top of platform-native **Security Scoped Bookmarks** (iOS) and **Storage Access Framework (SAF)** (Android).

<p align="center">
  <strong>
    <code>@daniele-rolli/capacitor-scoped-storage</code>
  </strong>
</p>

This plugin lets your app request a folder from the user and then perform safe file operations (read, write, append, delete, move, copy, stat, etc.) within that scope, with **persistent access across app restarts**.

👉 For a real-world setup, check out the [example-app](./example-app) with a full implementation.

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

## 📚 API Reference

<docgen-index>

* [`pickFolder()`](#pickfolder)
* [`writeFile(...)`](#writefile)
* [`appendFile(...)`](#appendfile)
* [`readFile(...)`](#readfile)
* [`mkdir(...)`](#mkdir)
* [`rmdir(...)`](#rmdir)
* [`readdir(...)`](#readdir)
* [`stat(...)`](#stat)
* [`exists(...)`](#exists)
* [`deleteFile(...)`](#deletefile)
* [`move(...)`](#move)
* [`copy(...)`](#copy)
* [`getUriForPath(...)`](#geturiforpath)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### pickFolder()

```typescript
pickFolder() => Promise<PickFolderResult>
```

**Returns:** <code>Promise&lt;<a href="#pickfolderresult">PickFolderResult</a>&gt;</code>

--------------------


### writeFile(...)

```typescript
writeFile(options: WriteOptions) => Promise<void>
```

| Param         | Type                                                  |
| ------------- | ----------------------------------------------------- |
| **`options`** | <code><a href="#writeoptions">WriteOptions</a></code> |

--------------------


### appendFile(...)

```typescript
appendFile(options: AppendOptions) => Promise<void>
```

| Param         | Type                                                  |
| ------------- | ----------------------------------------------------- |
| **`options`** | <code><a href="#writeoptions">WriteOptions</a></code> |

--------------------


### readFile(...)

```typescript
readFile(options: ReadOptions) => Promise<{ data: string; }>
```

| Param         | Type                                                |
| ------------- | --------------------------------------------------- |
| **`options`** | <code><a href="#readoptions">ReadOptions</a></code> |

**Returns:** <code>Promise&lt;{ data: string; }&gt;</code>

--------------------


### mkdir(...)

```typescript
mkdir(options: MkdirOptions) => Promise<void>
```

| Param         | Type                                                  |
| ------------- | ----------------------------------------------------- |
| **`options`** | <code><a href="#mkdiroptions">MkdirOptions</a></code> |

--------------------


### rmdir(...)

```typescript
rmdir(options: RmdirOptions) => Promise<void>
```

| Param         | Type                                                  |
| ------------- | ----------------------------------------------------- |
| **`options`** | <code><a href="#rmdiroptions">RmdirOptions</a></code> |

--------------------


### readdir(...)

```typescript
readdir(options: ReaddirOptions) => Promise<ReaddirResult>
```

| Param         | Type                                                      |
| ------------- | --------------------------------------------------------- |
| **`options`** | <code><a href="#readdiroptions">ReaddirOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#readdirresult">ReaddirResult</a>&gt;</code>

--------------------


### stat(...)

```typescript
stat(options: StatOptions) => Promise<StatResult>
```

| Param         | Type                                                |
| ------------- | --------------------------------------------------- |
| **`options`** | <code><a href="#statoptions">StatOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#statresult">StatResult</a>&gt;</code>

--------------------


### exists(...)

```typescript
exists(options: ExistsOptions) => Promise<ExistsResult>
```

| Param         | Type                                                    |
| ------------- | ------------------------------------------------------- |
| **`options`** | <code><a href="#existsoptions">ExistsOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#existsresult">ExistsResult</a>&gt;</code>

--------------------


### deleteFile(...)

```typescript
deleteFile(options: DeleteOptions) => Promise<void>
```

| Param         | Type                                                    |
| ------------- | ------------------------------------------------------- |
| **`options`** | <code><a href="#deleteoptions">DeleteOptions</a></code> |

--------------------


### move(...)

```typescript
move(options: MoveCopyOptions) => Promise<void>
```

| Param         | Type                                                        |
| ------------- | ----------------------------------------------------------- |
| **`options`** | <code><a href="#movecopyoptions">MoveCopyOptions</a></code> |

--------------------


### copy(...)

```typescript
copy(options: MoveCopyOptions) => Promise<void>
```

| Param         | Type                                                        |
| ------------- | ----------------------------------------------------------- |
| **`options`** | <code><a href="#movecopyoptions">MoveCopyOptions</a></code> |

--------------------


### getUriForPath(...)

```typescript
getUriForPath(options: UriForPathOptions) => Promise<UriForPathResult>
```

| Param         | Type                                                            |
| ------------- | --------------------------------------------------------------- |
| **`options`** | <code><a href="#uriforpathoptions">UriForPathOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#uriforpathresult">UriForPathResult</a>&gt;</code>

--------------------


### Interfaces


#### PickFolderResult

| Prop         | Type                                            |
| ------------ | ----------------------------------------------- |
| **`folder`** | <code><a href="#folderref">FolderRef</a></code> |


#### FolderRef

| Prop       | Type                | Description                                             |
| ---------- | ------------------- | ------------------------------------------------------- |
| **`id`**   | <code>string</code> | Android: tree URI; iOS: base64 security-scoped bookmark |
| **`name`** | <code>string</code> |                                                         |


#### WriteOptions

| Prop           | Type                                            |
| -------------- | ----------------------------------------------- |
| **`folder`**   | <code><a href="#folderref">FolderRef</a></code> |
| **`path`**     | <code>string</code>                             |
| **`data`**     | <code>string</code>                             |
| **`encoding`** | <code>'utf8' \| 'base64'</code>                 |
| **`mimeType`** | <code>string</code>                             |


#### ReadOptions

| Prop           | Type                                            |
| -------------- | ----------------------------------------------- |
| **`folder`**   | <code><a href="#folderref">FolderRef</a></code> |
| **`path`**     | <code>string</code>                             |
| **`encoding`** | <code>'utf8' \| 'base64'</code>                 |


#### MkdirOptions

| Prop            | Type                                            |
| --------------- | ----------------------------------------------- |
| **`folder`**    | <code><a href="#folderref">FolderRef</a></code> |
| **`path`**      | <code>string</code>                             |
| **`recursive`** | <code>boolean</code>                            |


#### RmdirOptions

| Prop            | Type                                            |
| --------------- | ----------------------------------------------- |
| **`folder`**    | <code><a href="#folderref">FolderRef</a></code> |
| **`path`**      | <code>string</code>                             |
| **`recursive`** | <code>boolean</code>                            |


#### ReaddirResult

| Prop          | Type                                                                                            |
| ------------- | ----------------------------------------------------------------------------------------------- |
| **`entries`** | <code>{ name: string; isDir: boolean; size?: number \| null; mtime?: number \| null; }[]</code> |


#### ReaddirOptions

| Prop         | Type                                            |
| ------------ | ----------------------------------------------- |
| **`folder`** | <code><a href="#folderref">FolderRef</a></code> |
| **`path`**   | <code>string</code>                             |


#### StatResult

| Prop        | Type                                            |
| ----------- | ----------------------------------------------- |
| **`uri`**   | <code>string</code>                             |
| **`size`**  | <code>number \| null</code>                     |
| **`mtime`** | <code>number \| null</code>                     |
| **`type`**  | <code>'file' \| 'directory' \| 'unknown'</code> |


#### StatOptions

| Prop         | Type                                            |
| ------------ | ----------------------------------------------- |
| **`folder`** | <code><a href="#folderref">FolderRef</a></code> |
| **`path`**   | <code>string</code>                             |


#### ExistsResult

| Prop              | Type                 |
| ----------------- | -------------------- |
| **`exists`**      | <code>boolean</code> |
| **`isDirectory`** | <code>boolean</code> |


#### ExistsOptions

| Prop         | Type                                            |
| ------------ | ----------------------------------------------- |
| **`folder`** | <code><a href="#folderref">FolderRef</a></code> |
| **`path`**   | <code>string</code>                             |


#### DeleteOptions

| Prop         | Type                                            |
| ------------ | ----------------------------------------------- |
| **`folder`** | <code><a href="#folderref">FolderRef</a></code> |
| **`path`**   | <code>string</code>                             |


#### MoveCopyOptions

| Prop            | Type                                            |
| --------------- | ----------------------------------------------- |
| **`folder`**    | <code><a href="#folderref">FolderRef</a></code> |
| **`from`**      | <code>string</code>                             |
| **`to`**        | <code>string</code>                             |
| **`overwrite`** | <code>boolean</code>                            |


#### UriForPathResult

| Prop      | Type                        |
| --------- | --------------------------- |
| **`uri`** | <code>string \| null</code> |


#### UriForPathOptions

| Prop         | Type                                            |
| ------------ | ----------------------------------------------- |
| **`folder`** | <code><a href="#folderref">FolderRef</a></code> |
| **`path`**   | <code>string</code>                             |


### Type Aliases


#### AppendOptions

<code><a href="#writeoptions">WriteOptions</a></code>

</docgen-api>

## 📄 License

[MIT](./LICENSE)
**Author:** Daniele Rolli
