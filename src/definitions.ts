export interface FolderRef {
  /** Android: tree URI; iOS: base64 security-scoped bookmark */
  id: string;
  name?: string;
}

export interface PickFolderResult { folder: FolderRef; }

export interface WriteOptions {
  folder: FolderRef;
  path: string;         // relative to folder
  data: string;         // utf8 or base64
  encoding?: 'utf8' | 'base64';
}

export type AppendOptions = WriteOptions

export interface ReadOptions {
  folder: FolderRef;
  path: string;
  encoding?: 'utf8' | 'base64';
}

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

export interface ExistsOptions {
  folder: FolderRef;
  path: string;
}
export interface ExistsResult { exists: boolean; isDirectory: boolean; }

export interface ReaddirOptions {
  folder: FolderRef;
  path?: string;
}
export interface ReaddirResult {
  entries: { name: string; isDir: boolean; size?: number | null; mtime?: number | null }[];
}

export interface StatOptions {
  folder: FolderRef;
  path: string;
}
export interface StatResult {
  uri?: string;         // content:// or file:// if available
  size?: number | null;
  mtime?: number | null;
  type: 'file' | 'directory' | 'unknown';
}

export interface UriForPathOptions {
  folder: FolderRef;
  path: string;
}
export interface UriForPathResult { uri: string | null; }

export interface ScopedStoragePlugin {
  pickFolder(): Promise<PickFolderResult>;

  // file data
  writeFile(options: WriteOptions): Promise<void>;
  appendFile(options: AppendOptions): Promise<void>;
  readFile(options: ReadOptions): Promise<{ data: string }>;

  // structure & metadata
  mkdir(options: MkdirOptions): Promise<void>;
  rmdir(options: RmdirOptions): Promise<void>;
  readdir(options: ReaddirOptions): Promise<ReaddirResult>;
  stat(options: StatOptions): Promise<StatResult>;
  exists(options: ExistsOptions): Promise<ExistsResult>;

  // path ops
  deleteFile(options: DeleteOptions): Promise<void>;
  move(options: MoveCopyOptions): Promise<void>;
  copy(options: MoveCopyOptions): Promise<void>;

  // convenience
  getUriForPath(options: UriForPathOptions): Promise<UriForPathResult>;
}
