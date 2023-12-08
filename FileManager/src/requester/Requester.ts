import axios from "axios";
import {Md5} from "ts-md5";
import {UploadRequestOptions} from "element-plus";
import {UploadProgressEvent} from "element-plus/es/components/upload/src/upload";
export const baseUrl = "http://localhost:8080";
const instance = axios.create({
    baseURL: baseUrl,
    timeout: 2000,
    headers: {
        "Content-Type": "application/json"
    }
});
instance.interceptors.request.use(
    (config) => {
        const token = window.sessionStorage.getItem("token");
        if (token){
            config.headers.authorization = token;
        }
        return config;
    }
)
instance.interceptors.response.use(
    (config) => {
        if (config.headers.authorization){
            window.sessionStorage.setItem("token", config.headers.authorization);
        }
        return config;
    }
)

export const getFileCount = () => {
    return instance.get("/api/file/count")
}
export const getFiles = (page: number) => {
    return instance.get("/api/file/get", {
        params: {
            page: page
        }
    })
}
export const searchFiles = (keywords: string, type: "NAME" | "ID" | "MD5", page: number = 1) => {
    return instance.get("/api/file/search", {
        params: {
            page: page,
            keywords: keywords,
            type: type
        }
    })
}
export const login = (username: string, password: string) => {
    const md5: Md5 = new Md5()
    md5.appendAsciiStr(password)
    const passwordMd5 = md5.end()
    return instance.post("/api/user/login", {
        username: username,
        password: passwordMd5
    })
}
export const register = (username: string, password: string) => {
    const md5: Md5 = new Md5()
    md5.appendAsciiStr(password)
    const passwordMd5 = md5.end()
    return instance.post("/api/user/register", {
        username: username,
        password: passwordMd5
    })
}
export const upload = (options: UploadRequestOptions, controller: AbortController) => {
    const param = new FormData();
    param.append("file", options.file);
    return instance.post("/api/file/upload", param, {
        headers: {
            "Content-Type": "multipart/form-data"
        },
        timeout: 0,
        onUploadProgress: (evt) => {
            const progressEvent: ProgressEvent = evt.event as ProgressEvent;
            const uploadEvent: UploadProgressEvent = progressEvent as UploadProgressEvent;
            uploadEvent.percent = (evt.progress as number) * 100;
            options.onProgress(uploadEvent);
        },
        signal: controller.signal
    })
}
export const deleteFile = (fileId: number) => {
    return instance.post("/api/file/remove", {id: fileId})
}
export const getFileDetails = (fileId: number) => {
    return instance.get("/api/file/details", {params: {"id": fileId}});
}