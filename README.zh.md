# File Sharing Station V3 / 文件分享站 V3

一个自托管的文件分享与管理 Web 应用。这是 **v3 版本前端**，使用 Vue 3 + Nuxt UI 重构，对接 FileManager Spring Boot 后端。

A self-hosted file sharing and management web application. This is the **v3 frontend**, rebuilt with Vue 3 + Nuxt UI, connecting to the FileManager Spring Boot backend.

> 语言: **中文** | [English](./README.md)

---

## 功能特性

- **文件浏览** — 表格视图，支持排序、搜索和分页；面包屑路径导航
- **文件上传** — 分片上传 + MD5 去重，拖拽上传文件和文件夹，通过 File System Access API 实现目录树上传
- **文件管理** — 新建文件夹、删除文件/文件夹、安全链接下载、SHA-512 完整性校验
- **P2P 传输** — 基于 WebRTC 的点对点文件传输，WebSocket 信令
- **身份认证** — JWT 令牌认证、浏览器指纹自动登录、管理员邀请码注册
- **用户偏好** — 持久化排序设置、修改用户名/密码
- **管理员** — 生成邀请码、管理所有文件

## 技术栈

| 层级 | 技术 |
|---|---|
| 框架 | Vue 3（Composition API、`<script setup>`） |
| 语言 | TypeScript 6 |
| 构建 | Vite 8 |
| UI 组件库 | Nuxt UI v4 |
| CSS | Tailwind CSS 4 |
| 状态管理 | Pinia 3 |
| 代码检查/格式化 | oxlint + oxfmt + ESLint |

## 环境要求

- **Node.js** >= 20.19.0 或 >= 22.12.0
- **Bun**（包管理器）
- **FileManager 后端** 已启动 — 参见后端 README（`master` 分支，同一仓库）

## 快速开始

```bash
# 安装依赖
bun install

# 启动开发服务器
bun dev

# 生产构建
bun run build

# 代码检查
bun lint

# 代码格式化
bun format
```

开发服务器默认运行在 `http://localhost:5173`。API 请求通过 Vite 代理转发到后端，可在 `vite.config.ts` 中配置代理目标地址。

## 后端对接

本前端连接 **FileManager** Spring Boot 后端（同一仓库，`master` 分支）。关键 API 信息：

- **基础路径:** `/api`
- **认证方式:** JWT 令牌通过 `Add-Authorization` 响应头下发，客户端通过 `Authorization` 请求头回传
- **代理:** Vite 开发服务器将 `/api` 请求代理到后端（默认 `http://127.0.0.1:8080`）

API 文档和部署说明请参见后端 README。

## 相关分支

| 分支 | 说明 |
|---|---|
| `master` | 后端（Spring Boot） |
| `v2-web` | V2 前端（Vue 3 + Element Plus） |
| `v3-web`（当前） | V3 前端（Vue 3 + Nuxt UI） |

## 许可证

MIT — 详见 [LICENSE](./LICENSE)
