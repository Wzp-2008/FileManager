import axios from "axios";

const instance = axios.create({
    baseURL: "http://localhost:8080",
    timeout: 2000,
    headers: {
        "Content-Type": "application/json"
    }
})

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