# File Sharing Station V3 / 文件分享站 V3

A self-hosted file sharing and management web application. This is the **v3 frontend**, rebuilt with Vue 3 + Nuxt UI, connecting to the FileManager Spring Boot backend.

一个自托管的文件分享与管理 Web 应用。这是 **v3 版本前端**，使用 Vue 3 + Nuxt UI 重构，对接 FileManager Spring Boot 后端。

> Language: [中文](./README.zh.md) | **English**

---

## Features

- **File browsing** — table view with sort, search, and pagination; breadcrumb path navigation
- **File upload** — chunked upload with MD5 deduplication, drag-and-drop files and folders, directory-tree upload via File System Access API
- **File management** — create folders, delete files/folders, download with secure links, SHA-512 integrity verification
- **P2P transfer** — WebRTC-based peer-to-peer file transfer with WebSocket signaling
- **Authentication** — JWT token auth, browser fingerprint auto-login, admin invite code registration
- **User preferences** — persistent sort settings, change username/password
- **Admin** — generate invite codes, manage all files

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Vue 3 (Composition API, `<script setup>`) |
| Language | TypeScript 6 |
| Build | Vite 8 |
| UI Library | Nuxt UI v4 |
| CSS | Tailwind CSS 4 |
| State | Pinia 3 |
| Lint / Format | oxlint + oxfmt + ESLint |

## Prerequisites

- **Node.js** >= 20.19.0 or >= 22.12.0
- **Bun** (package manager)
- **FileManager backend** running — see backend README (`master` branch, same repo)

## Getting Started

```bash
# Install dependencies
bun install

# Start dev server
bun dev

# Build for production
bun run build

# Lint
bun lint

# Format
bun format
```

The dev server runs on `http://localhost:5173` by default. API requests are proxied to the backend — configure the proxy target in `vite.config.ts`.

## Backend Integration

This frontend connects to the **FileManager** Spring Boot backend (same repo, `master` branch). Key API facts:

- **Base path:** `/api`
- **Auth:** JWT token delivered via `Add-Authorization` response header, sent back via `Authorization` request header
- **Proxy:** Vite dev server proxies `/api` to the backend (default `http://127.0.0.1:8080`)

See the backend README for API documentation and deployment.

## Related Branches

| Branch | Description |
|---|---|
| `master` | Backend (Spring Boot) |
| `v2-web` | V2 frontend (Vue 3 + Element Plus) |
| `v3-web` (current) | V3 frontend (Vue 3 + Nuxt UI) |

## License

MIT — see [LICENSE](./LICENSE)
