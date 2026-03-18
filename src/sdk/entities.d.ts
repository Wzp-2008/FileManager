/**
 * 文件类型（FILE为文件、FOLDER为文件夹）
 */
export type RawFileType = "FILE" | "FOLDER";
/**
 * 用户类型（user为普通用户、admin为管理员）
 */
export type UserAuth = "user" | "admin";

/**
 * 文件对象（可表示文件/文件夹）
 */
export interface RawFile {
  /**
   * 文件/文件夹ID
   */
  id: number;
  /**
   * 文件/文件夹名
   */
  name: string;
  /**
   * 文件扩展名（文件夹为undefined）
   */
  ext?: string;
  /**
   * 文件大小（文件夹为-1）
   */
  size: number;
  /**
   * 文件所有者ID
   */
  owner: number;
  /**
   * 文件的文件夹ID / 文件夹的父级ID（根目录为-1）
   */
  parent: number;
  /**
   * 文件/文件夹的上传时间
   */
  time: string;
  /**
   * 文件类型
   * @see RawFileType
   */
  type: RawFileType;
  /**
   * 文件mime
   */
  mime: "folder" | string;
}

/**
 * 带有所有者名称的文件对象（就是比RawFile多了一个所有者名称）
 * @see RawFile
 */
export interface NamedRawFile extends RawFile {
  /**
   * 文件/文件夹所有者名称
   */
  ownerName: string;
}

/**
 * 文件对象（只能表示文件）
 */
export interface FileObject {
  /**
   * 文件ID
   */
  id: number;
  /**
   * 文件名
   */
  name: string;
  /**
   * 文件扩展名
   */
  ext: string;
  /**
   * 文件的<a href="https://developer.mozilla.org/zh-CN/docs/Glossary/MIME_type">MIME</a>类型
   * @see https://developer.mozilla.org/zh-CN/docs/Glossary/MIME_type
   */
  mime: string;
  /**
   * 文件的sha512哈希值
   */
  hash: string;
  /**
   * 文件的上传者ID
   */
  uploader: number;
  /**
   * 文件所属的文件夹ID（根目录为-1）
   */
  folder: number;
  /**
   * 文件大小（字节为单位）
   */
  size: number;
  /**
   * 文件的上传时间
   */
  uploadTime: string;
}

/**
 * 文件夹对象（只能表示文件夹）
 */
export interface FolderObject {
  /**
   * 文件夹ID
   */
  id: number;
  /**
   * 文件夹名称
   */
  name: string;
  /**
   * 文件夹的父文件夹ID
   */
  parent: number;
  /**
   * 文件夹创建者
   */
  creator: number;
  /**
   * 文件夹创建时间
   */
  createTime: string;
}

/**
 * 用户设置
 */
export interface UserPrefs {
  sortField?: SortField;
  sortReverse?: boolean;
  userId?: number;
}

/**
 * 用户对象
 */
export interface User {
  /**
   * 用户ID
   */
  id: number;
  /**
   * 用户名
   */
  name: string;
  /**
   * 用户类型
   * @see UserAuth
   */
  auth: UserAuth;
  /**
   * 用户是否被封禁<em><s>（被封禁了你也见不到就是了）</s></em>
   */
  banned: boolean;
  /**
   * 用户设置
   */
  prefs?: UserPrefs;
}

/**
 * 排序列
 */
export type SortField =
  | "ID" // ID（默认）
  | "NAME" // 文件名
  | "EXT" // 文件扩展名
  | "TIME" // 文件上传时间
  | "UPLOADER" // 上传者
  | "DOWNLOAD_COUNT" // 下载数量
  | "SIZE"; // 文件大小
