import Axios, { type AxiosProgressEvent, CanceledError } from "axios";
import type { AxiosInstance, AxiosResponse } from "axios";
import type {
  ChunkCheckEntry,
  FileObject,
  FolderObject,
  NamedRawFile,
  RawFile,
  RawFileType,
  SortField,
  User,
  UserPrefs,
} from "./entities";
import { ElMessage } from "element-plus";
import type { Pager, Result } from "./network";
import type { UserLoginRequest, UserRegisterRequest } from "./request";
import { hashPassword } from "./utils";
import { ConcurrencyManager } from "axios-concurrency";
import { hash } from "spark-md5";
/**
 * 文件分享站主SDK
 */
class FileManagerSdk {
  /**
   * Axios实例用于发送请求（这个对象是私有对象，不能在外界调用）
   * @private
   */
  #requester: AxiosInstance;
  /**
   * 后端基础URL
   * @private
   */
  #baseUrl: string;

  /**
   * 构造方法
   * @param baseURL 后端基础url（默认为/）
   */
  constructor(baseURL: string = "/") {
    this.#baseUrl = baseURL;
    this.#requester = Axios.create({ baseURL });
    const MAX_CONCURRENT_REQUESTS = 5;
    /* @ts-ignore */
    ConcurrencyManager(this.#requester, MAX_CONCURRENT_REQUESTS);
    // 请求拦截器，用来加token
    this.#requester.interceptors.request.use((request) => {
      const token = localStorage.getItem("token");
      if (token) {
        request.headers["Authorization"] = token;
      }
      return request;
    });
    //第一层响应拦截器，用于判断返回的数据是否正确，并且获取从服务端发回的token存入localStorage
    this.#requester.interceptors.response.use(
      (response) => {
        const setAuth = response.headers["add-authorization"];
        if (setAuth) {
          localStorage.setItem("token", setAuth);
        }
        const responseData = response.data as Result<any>;
        if (199 < responseData.status && 300 > responseData.status) {
          response.data = responseData.data;
          return response;
        }
        return Promise.reject(responseData.msg);
      },
      (err) => {
        if (err instanceof CanceledError) {
          throw err;
        }
        console.error("网络错误！", err);
        ElMessage.error("请求数据失败，网络错误！");
      },
    );
    this.#requester.interceptors.response.use(null, (err: string) => {
      /**
       * 可以在这里编写报错时触发的逻辑，err是错误原因文本
       */
      return Promise.reject(err);
    });
  }

  /**
   * 获取当前登录的用户信息（登陆后才能正常获取，不然会报错）
   */
  async getUserInformation(): Promise<AxiosResponse<User>> {
    return this.#requester.get("/api/user/info");
  }

  /**
   * 获取单个用户
   * @param id 要获取的用户ID
   */
  async getUser(id: number): Promise<AxiosResponse<User>> {
    return this.#requester.get("/api/user/info/" + id);
  }

  /**
   * 登录
   * @param data 登录的请求信息
   * @see UserLoginRequest
   */
  async login(data: UserLoginRequest): Promise<AxiosResponse<User>> {
    data = hashPassword(data);
    return this.#requester.post("/api/user/login", data);
  }

  /**
   * 注册
   * @param data 注册的请求信息
   * @see UserRegisterRequest
   */
  async register(data: UserRegisterRequest): Promise<AxiosResponse<User>> {
    data = hashPassword(data);
    return this.#requester.put("/api/user/register", data);
  }

  /**
   * 分页获取文件
   * @param num 每一页的数量
   * @param page 页数
   * @param folder 父文件夹（默认为-1）
   * @param searchContent 搜索内容（默认为空）
   * @param sort 排序方式（默认为ID）
   * @param reverse 反向排序（默认为false）
   * @param cancelSignal 取消信号
   */
  async getFolderFilesPager(
    num: number,
    page: number,
    folder: number = -1,
    searchContent: string = "",
    sort: SortField = "ID",
    reverse: boolean = false,
    cancelSignal: AbortSignal | null = null,
  ): Promise<AxiosResponse<Pager<NamedRawFile>>> {
    return this.#requester.get("/api/file/get", {
      params: { num, page, folder, keywords: searchContent, sort, reverse },
      signal: cancelSignal ? cancelSignal : undefined,
    });
  }

  /**
   * 通过路径获取文件对象
   * @param path 文件路径
   */
  async getFileByPath(path: string): Promise<AxiosResponse<RawFile>> {
    return this.#requester.get("/api/file/path/resolve", {
      params: { path },
    });
  }

  async getPathById(
    id: number,
    type: RawFileType,
  ): Promise<AxiosResponse<string>> {
    return this.#requester.get(`/api/file/path/${id}`, { params: { type } });
  }

  /**
   * 获取文件夹详细信息
   * @param id 文件夹ID
   */
  async getFolderInformation(id: number): Promise<AxiosResponse<NamedRawFile>> {
    return this.#requester.get("/api/file/get/folder", {
      params: { id },
    });
  }

  /**
   * 获取文件下载链接后面的ID
   * @param id 文件ID
   */
  async genFileDownloadLink(id: number): Promise<AxiosResponse<string>> {
    return this.#requester.get("/api/file/link", { params: { id } });
  }

  /**
   * 获取文件详细信息
   * @param id 文件ID
   */
  async getFileDetail(id: number): Promise<AxiosResponse<FileObject>> {
    return this.#requester.get("/api/file/detail/file", {
      params: { id },
    });
  }

  /**
   * 删除文件/文件夹
   * @param id 文件/文件夹ID
   * @param type 删除的类型
   */
  async deleteFile(
    id: number,
    type: RawFileType,
  ): Promise<AxiosResponse<void>> {
    return this.#requester.delete("/api/file/rm", {
      params: { id, type },
    });
  }

  /**
   * 文件上传接口
   * @param file 上传的文件对象
   * @param currentFolderId 上传到的文件夹ID（根目录为-1）
   * @param abortSignal 取消信号
   * @param onProgress 上传进度事件监听器
   */
  async upload(
    file: File,
    currentFolderId: number,
    abortSignal: AbortSignal = new AbortSignal(),
    onProgress: (progressEvent: AxiosProgressEvent) => void,
  ): Promise<AxiosResponse<FileObject>> {
    const response = (await this.#requester
      .post("/api/file/upload/check", {
        name: file.name,
        folderId: currentFolderId,
      })
      .catch((e) => {
        ElMessage.error(`上传文件${file.name}失败，${e}`);
      })) as AxiosResponse;
    if (response.data) {
      const data = new FormData();
      data.append("file", file);
      // 使用底层onDrop事件实现文件夹和文件选择器
      return this.#requester.put("/api/file/upload", data, {
        params: { folder: currentFolderId },
        onUploadProgress: (event) => {
          onProgress(event);
        },
        signal: abortSignal,
      });
    }
    throw response.data.msg;
  }

  /**
   * 创建文件夹
   * @param name 文件夹名称
   * @param parentId 父级文件夹ID（根目录为-1）
   * @param existsReturn 当文件夹存在时返回已存在的文件夹
   */
  async mkdir(
    name: string,
    parentId: number,
    existsReturn: boolean = false,
  ): Promise<AxiosResponse<FolderObject>> {
    return this.#requester.post("/api/file/mkdir", {
      parent: parentId,
      name,
      existsReturn,
    });
  }

  /**
   * 通过genFileDownloadLink方法获取到的下载ID获取下载链接
   * @param randomString
   */
  getFullDownloadLink(randomString: string): string {
    return this.#baseUrl + "api/file/download/" + randomString;
  }

  /**
   * 登出（从localStore中删除token）
   */
  logout(fingerprint: string) {
    this.removeFingerprint(fingerprint).finally(() => {
      localStorage.removeItem("token");
    });
  }

  /**
   * 打开下载链接
   * @param fileId 文件ID
   */
  async openFullDownloadLink(fileId: number) {
    await this.genFileDownloadLink(fileId).then((resp) => {
      window.open(this.getFullDownloadLink(resp.data));
    });
  }

  /**
   * 更新用户设置
   * @param prefs 用户设置
   */
  async updateUserPrefs(prefs: Partial<UserPrefs>) {
    return this.#requester.post("/api/user/prefs", prefs);
  }

  /**
   * 尝试使用浏览器指纹登录
   * @param fingerprint 浏览器指纹
   */
  async tryLoginWithFingerprint(
    fingerprint: string,
  ): Promise<AxiosResponse<User>> {
    return this.#requester.get("/api/user/fingerprint/login", {
      params: { fingerprint },
    });
  }

  /**
   * 保存当前用户浏览器指纹
   * @param fingerprint 浏览器指纹
   */
  async saveFingerprint(fingerprint: string): Promise<AxiosResponse<boolean>> {
    return this.#requester.post("/api/user/fingerprint/save", {
      fingerprint,
    });
  }

  /**
   * 尝试删除当前用户保存的指纹
   * @param fingerprint
   */
  async removeFingerprint(
    fingerprint: string,
  ): Promise<AxiosResponse<boolean>> {
    return this.#requester.delete("/api/user/fingerprint/tryRemove", {
      params: { fingerprint },
    });
  }

  /**
   * 获取邀请码（需要管理员权限）
   */
  async getInviteCode(): Promise<AxiosResponse<string>> {
    return this.#requester.get("/api/user/invite");
  }

  /**
   * 修改用户名
   * @param newName 新的用户名
   */
  async changeUsername(newName: string): Promise<AxiosResponse<boolean>> {
    return this.#requester.post("/api/user/username/" + newName);
  }

  /**
   * 修改密码
   * @param oldPassword 旧密码
   * @param newPassword 新密码
   */
  async changePassword(
    oldPassword: string,
    newPassword: string,
  ): Promise<AxiosResponse<boolean>> {
    return this.#requester.post("/api/user/password", {
      oldPassword: hash(oldPassword),
      newPassword: hash(newPassword),
    });
  }

  /**
   * 检查需要上传哪些区块
   * @param chunksHash 所有区块的哈希（MD5）列表
   * @param cancelSignal 取消信号，通过AbortController创建
   */
  async checkChunks(
    chunksHash: string[],
    cancelSignal: AbortSignal,
  ): Promise<AxiosResponse<ChunkCheckEntry[]>> {
    return this.#requester.post("/api/file/chunk/check", chunksHash, {
      signal: cancelSignal,
    });
  }

  /**
   * 上传单个区块
   * @param chunk 区块
   * @param cancelSignal 取消信号，通过AbortController创建
   */
  async uploadChunk(
    chunk: Blob,
    cancelSignal: AbortSignal,
  ): Promise<AxiosResponse<number>> {
    const form = new FormData();
    form.append("block", chunk);
    return this.#requester.post("/api/file/chunk/upload", form, {
      signal: cancelSignal,
    });
  }

  /**
   * 将已经上传的区块合并成一个文件
   * @param filename 文件名
   * @param chunks 区块ID列表
   * @param folderId 文件夹ID（默认为-1，即根目录）
   * @param cancelSignal 取消信号，通过AbortController创建
   */
  async saveChunksToFile(
    filename: string,
    chunks: number[],
    folderId: number = -1,
    cancelSignal: AbortSignal,
  ): Promise<AxiosResponse<FileObject>> {
    return this.#requester.put(
      "/api/file/chunk/save",
      {
        filename,
        chunks,
        folderId,
      },
      { signal: cancelSignal },
    );
  }
}

/**
 * ROOT对象（即根文件夹对象），用于在获取根文件夹的目录中的文件时传入
 */
export const ROOT: NamedRawFile = {
  type: "FOLDER",
  id: -1,
  name: "",
  owner: -1,
  ownerName: "",
  size: -1,
  time: "",
  mime: "folder",
  parent: -1,
};

export default FileManagerSdk;
