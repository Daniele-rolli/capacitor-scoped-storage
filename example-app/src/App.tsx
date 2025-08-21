import { useState } from 'react';
import { FolderRef, ScopedStorage } from 'capacitor-scoped-storage';

export default function App() {
  const [folder, setFolder] = useState<FolderRef | null>(null);
  const [log, setLog] = useState("");

  const addLog = (msg: string, data?: any) => {
    setLog(l => `${l}\n${msg} ${data ? JSON.stringify(data) : ""}`);
  };

  const doPick = async () => {
    try {
      const res = await ScopedStorage.pickFolder();
      setFolder(res.folder);
      addLog("Picked folder:", res.folder);
    } catch (e) {
      addLog("pickFolder error", e);
    }
  };

  const doWrite = async () => {
    if (!folder) return;
    await ScopedStorage.writeFile({
      folder,
      path: "test/hello.txt",
      data: "Hello World!",
      encoding: "utf8"
    });
    addLog("writeFile done");
  };

  const doAppend = async () => {
    if (!folder) return;
    await ScopedStorage.appendFile({
      folder,
      path: "test/hello.txt",
      data: "\nAppended line",
      encoding: "utf8"
    });
    addLog("appendFile done");
  };

  const doRead = async () => {
    if (!folder) return;
    const res = await ScopedStorage.readFile({ folder, path: "test/hello.txt", encoding: "utf8" });
    addLog("readFile:", res.data);
  };

  const doMkdir = async () => {
    if (!folder) return;
    await ScopedStorage.mkdir({ folder, path: "deep/dir/structure", recursive: true });
    addLog("mkdir done");
  };

  const doRmdir = async () => {
    if (!folder) return;
    await ScopedStorage.rmdir({ folder, path: "deep", recursive: true });
    addLog("rmdir done");
  };

  const doReaddir = async () => {
    if (!folder) return;
    const res = await ScopedStorage.readdir({ folder, path: "test" });
    addLog("readdir:", res.entries);
  };

  const doStat = async () => {
    if (!folder) return;
    const res = await ScopedStorage.stat({ folder, path: "test/hello.txt" });
    addLog("stat:", res);
  };

  const doExists = async () => {
    if (!folder) return;
    const res = await ScopedStorage.exists({ folder, path: "test/hello.txt" });
    addLog("exists:", res);
  };

  const doDelete = async () => {
    if (!folder) return;
    await ScopedStorage.deleteFile({ folder, path: "test/hello.txt" });
    addLog("deleteFile done");
  };

  const doMove = async () => {
    if (!folder) return;
    await ScopedStorage.move({ folder, from: "test/hello.txt", to: "test/moved.txt", overwrite: true });
    addLog("move done");
  };

  const doCopy = async () => {
    if (!folder) return;
    await ScopedStorage.copy({ folder, from: "test/moved.txt", to: "test/copied.txt", overwrite: true });
    addLog("copy done");
  };

  const doUri = async () => {
    if (!folder) return;
    const res = await ScopedStorage.getUriForPath({ folder, path: "test/copied.txt" });
    addLog("getUriForPath:", res.uri);
  };

  return (
    <div style={{ padding: 20 }}>
      <h1>Scoped Storage Test</h1>
      <button onClick={doPick}>Pick Folder</button>
      <button onClick={doWrite}>Write File</button>
      <button onClick={doAppend}>Append File</button>
      <button onClick={doRead}>Read File</button>
      <button onClick={doMkdir}>Mkdir</button>
      <button onClick={doRmdir}>Rmdir</button>
      <button onClick={doReaddir}>Readdir</button>
      <button onClick={doStat}>Stat</button>
      <button onClick={doExists}>Exists</button>
      <button onClick={doDelete}>Delete</button>
      <button onClick={doMove}>Move</button>
      <button onClick={doCopy}>Copy</button>
      <button onClick={doUri}>Get URI</button>
      <pre style={{ background: "#eee", padding: 10, whiteSpace: "pre-wrap" }}>{log}</pre>
    </div>
  );
}
