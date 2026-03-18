/**
 * This Module was writing for the fucking W3C and Webkit and TypeScript!!!!
 * @author wzp
 * @since 2025/04/28
 */
import {
  ElMessage,
  ElNotification,
  type UploadStatus,
  type UploadUserFile,
} from "element-plus";
import type { FileObject } from "./sdk/entities";
import type { AxiosResponse } from "axios";
import FileManagerSdk from "./sdk";
import { wrap, type WrapValue } from "./sdk/utils.ts";

export interface BaseEntry {
  kind: "file" | "folder";

  getName(): string;
}

export interface BaseFile extends BaseEntry {
  kind: "file";

  toFile(): Promise<File>;
}

export class FileWrapper implements BaseFile {
  file: File;
  kind: "file" = "file";

  constructor(file: File) {
    this.file = file;
  }

  async toFile(): Promise<File> {
    return this.file;
  }

  getName(): string {
    return this.file.name;
  }
}

export class FileHandleWrapper implements BaseFile {
  file: FileSystemFileHandle;
  kind: "file" = "file";

  constructor(file: FileSystemFileHandle) {
    this.file = file;
  }

  async toFile(): Promise<File> {
    return this.file.getFile();
  }

  getName(): string {
    return this.file.name;
  }
}

export class WebKitFileHandleWrapper implements BaseFile {
  file: FileSystemFileEntry;
  kind = "file" as const;

  constructor(file: FileSystemFileEntry) {
    this.file = file;
  }

  toFile(): Promise<File> {
    return new Promise((resolve, reject) => {
      this.file.file(
        (file) => {
          resolve(file);
        },
        (err) => {
          reject(err);
        },
      );
    });
  }

  getName(): string {
    return this.file.name;
  }
}

export interface BaseFolder extends BaseEntry {
  kind: "folder";

  getChildren(): Promise<BaseEntry[]>;
}

export class FileHandleFolderWrapper implements BaseFolder {
  kind = "folder" as const;

  constructor(public folder: FileSystemDirectoryHandle) {}

  async getChildren(): Promise<BaseEntry[]> {
    const result = [];

    for await (let value of this.folder.values()) {
      result.push(
        value.kind === "file"
          ? new FileHandleWrapper(value)
          : new FileHandleFolderWrapper(value),
      );
    }
    return result;
  }

  getName(): string {
    return this.folder.name;
  }
}

export class WebKitFileHandleFolderWrapper implements BaseFolder {
  kind: "folder" = "folder";

  constructor(public folder: FileSystemDirectoryEntry) {}

  getChildren(): Promise<BaseEntry[]> {
    return new Promise((resolve, reject) => {
      const reader = this.folder.createReader();
      reader.readEntries(
        (entries) => {
          resolve(
            entries.map((e) =>
              e.isFile
                ? new WebKitFileHandleWrapper(e as FileSystemFileEntry)
                : new WebKitFileHandleFolderWrapper(
                    e as FileSystemDirectoryEntry,
                  ),
            ),
          );
        },
        (err) => {
          reject(err);
        },
      );
    });
  }

  getName(): string {
    return this.folder.name;
  }
}

export interface BaseFileUploadTask<T> extends UploadUserFile {
  start(sdk: FileManagerSdk, folderId: number): Promise<T>;

  cancel(): void;
}

export abstract class AbstractUploadTask<T> implements BaseFileUploadTask<T> {
  name: string;
  percentage: number;
  status: UploadStatus;
  abortController: AbortController;

  protected constructor(entry: BaseEntry) {
    this.name = entry.getName();
    this.percentage = 0;
    this.status = "ready";
    this.abortController = new AbortController();
  }

  abstract start(sdk: FileManagerSdk, folderId: number): Promise<T>;

  cancel(): void {
    this.abortController.abort();
  }
}

export type ResponseFile = AxiosResponse<FileObject>;
export type ResponseFolder = ResponseFile[];

export class FileUploadTask extends AbstractUploadTask<ResponseFile> {
  file: BaseFile;

  constructor(file: BaseFile) {
    super(file);
    this.file = file;
  }

  async start(sdk: FileManagerSdk, folderId: number): Promise<ResponseFile> {
    this.status = "uploading";
    return sdk
      .upload(
        await this.file.toFile(),
        folderId,
        this.abortController.signal,
        (e) => {
          this.percentage = Math.floor(e.progress ? e.progress * 100 : 0);
        },
      )
      .then((e) => {
        this.status = "success";
        this.percentage = 100;
        return e;
      })
      .catch((err) => {
        this.status = "fail";
        throw err;
      });
  }
}

export class FolderUploadTask extends AbstractUploadTask<ResponseFolder> {
  folder: BaseFolder;
  private readonly currentStatusList: WrapValue<number>[];

  constructor(folder: BaseFolder) {
    super(folder);
    this.folder = folder;
    this.currentStatusList = [];
  }

  private updateFullProgress() {
    if (this.currentStatusList.length === 0) {
      this.percentage = 0;
      return;
    }
    let sumOfFileProgress = 0;
    for (let singleFileStatus of this.currentStatusList) {
      sumOfFileProgress += singleFileStatus.current;
    }
    this.percentage = Math.floor(
      (sumOfFileProgress / this.currentStatusList.length) * 100,
    );
  }

  private async uploadFolder(
    sdk: FileManagerSdk,
    entry: BaseFolder,
    parentId: number,
  ): Promise<ResponseFolder> {
    if (this.abortController.signal.aborted) {
      throw this.abortController.signal.reason;
    }
    const currentLayerCreatedResponse = await sdk.mkdir(
      entry.getName(),
      parentId,
      true,
    );
    const currentLayerId = currentLayerCreatedResponse.data.id;
    const children = await entry.getChildren();
    if (this.abortController.signal.aborted) {
      throw this.abortController.signal.reason;
    }
    const results = await Promise.allSettled<Promise<ResponseFolder>[]>(
      children.map(async (child) => {
        if (this.abortController.signal.aborted) {
          throw this.abortController.signal.reason;
        }
        if (child.kind === "folder") {
          return this.uploadFolder(sdk, child as BaseFolder, currentLayerId);
        }
        const file = await (child as BaseFile).toFile();
        const currentFileProgress = wrap(0);
        this.currentStatusList.push(currentFileProgress);
        return [
          await sdk.upload(
            file,
            currentLayerId,
            this.abortController.signal,
            (e) => {
              currentFileProgress.current = e.progress ? e.progress : 0;
              this.updateFullProgress();
            },
          ),
        ];
      }),
    );
    return results
      .filter((e) => e.status === "fulfilled")
      .map((e) => e.value)
      .flat() as ResponseFolder;
  }

  async start(sdk: FileManagerSdk, folderId: number): Promise<ResponseFolder> {
    this.status = "uploading";
    return this.uploadFolder(sdk, this.folder, folderId)
      .then((e) => {
        this.status = "success";
        this.percentage = 100;
        return e;
      })
      .catch((err) => {
        this.status = "fail";
        throw err;
      });
  }
}

export const useDataTransferItemHandler = (): ((
  item: DataTransferItem,
) => Promise<BaseEntry>) => {
  if (!!DataTransferItem.prototype.getAsFileSystemHandle) {
    return async (item: DataTransferItem) => {
      const handle = await item.getAsFileSystemHandle();
      if (handle) {
        if (handle.kind === "file") {
          return new FileHandleWrapper(handle as FileSystemFileHandle);
        }
        return new FileHandleFolderWrapper(handle as FileSystemDirectoryHandle);
      }
      throw `Failed to read item`;
    };
  }
  if (!!DataTransferItem.prototype.webkitGetAsEntry) {
    return async (item: DataTransferItem) => {
      const webKitEntry = item.webkitGetAsEntry();
      if (webKitEntry) {
        if (webKitEntry.isFile) {
          return new WebKitFileHandleWrapper(
            webKitEntry as FileSystemFileEntry,
          );
        }
        return new WebKitFileHandleFolderWrapper(
          webKitEntry as FileSystemDirectoryEntry,
        );
      }
      throw `Failed to read item`;
    };
  }

  return async (item: DataTransferItem) => {
    if (item.type === "") {
      ElMessage.error("浏览器不支持文件夹上传！");
      throw "folder upload unsupported";
    }
    const asFile = item.getAsFile();
    if (asFile) {
      return new FileWrapper(asFile);
    }
    throw `Failed to read item`;
  };
};

export const createUploadTask = (
  entry: BaseEntry,
): BaseFileUploadTask<ResponseFile | ResponseFolder> => {
  return entry.kind === "file"
    ? new FileUploadTask(entry as BaseFile)
    : new FolderUploadTask(entry as BaseFolder);
};

const getOrCreateInputElement = () => {
  let inputElement = document.querySelector(
    "#hidden-input",
  ) as HTMLInputElement;
  if (inputElement) return inputElement;
  inputElement = document.createElement("input");
  inputElement.id = "hidden-input";
  inputElement.type = "file";
  inputElement.style.display = "none";
  document.body.appendChild(inputElement);
  return inputElement;
};

export const useFileSelect = (): (() => Promise<BaseFile[]>) => {
  if (!!globalThis.showDirectoryPicker) {
    return async () => {
      return [...(await showOpenFilePicker({ multiple: true }))].map(
        (e) => new FileHandleWrapper(e),
      );
    };
  }
  const inputElement = getOrCreateInputElement();
  return () => {
    return new Promise((resolve) => {
      inputElement.multiple = true;
      inputElement.webkitdirectory = false;
      const listener = () => {
        inputElement.files
          ? resolve([...inputElement.files].map((e) => new FileWrapper(e)))
          : resolve([]);
      };
      inputElement.addEventListener("change", listener, { once: true });
      inputElement.addEventListener("cancel", () => {
        inputElement.removeEventListener("change", listener);
        resolve([]);
      });
      inputElement.click();
    });
  };
};

export const useFolderSelect = (): (() => Promise<BaseFolder[]>) => {
  if (!!globalThis.showDirectoryPicker) {
    return async () => [
      new FileHandleFolderWrapper(await showDirectoryPicker()),
    ];
  }
  ElNotification.error({
    title: "浏览器不兼容",
    message:
      "由于傻逼Chrome的问题，你不能使用点击按钮上传文件夹功能（可以拖进来试试），但还是建议使用Chrome进行操作",
    position: "top-left",
  });
  // TODO FUCK YOU CHROME
  // const inputElement = getOrCreateInputElement();
  return () => {
    ElMessage.error(
      "你的浏览器不支持文件夹上传功能（当然你可以试试拖进来），建议使用Chrome浏览器（去你妈的）进行访问！",
    );
    return new Promise((res) => res([]));
    /*return new Promise((resolve) => {
      inputElement.multiple = false;
      inputElement.webkitdirectory = true;
      const listener = () => {
        resolve(
          [...inputElement.webkitEntries].map(
            (e) =>
              new WebKitFileHandleFolderWrapper(e as FileSystemDirectoryEntry),
          ),
        );
      };
      inputElement.addEventListener("change", listener, { once: true });
      inputElement.addEventListener("cancel", () => {
        inputElement.removeEventListener("change", listener);
        resolve([]);
      });
      inputElement.click();
    });*/
  };
};
