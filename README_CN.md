# 文件分享站

> 一个自托管的文件管理与分享平台，支持断点续传的分块上传、文件夹组织、用户认证与点对点文件传输。

[![License](https://img.shields.io/github/license/Wzp-2008/FileManager)](LICENSE)
[![Release](https://img.shields.io/github/v/release/Wzp-2008/FileManager)](https://github.com/Wzp-2008/FileManager/releases)
[![Docker Pulls](https://img.shields.io/docker/pulls/wzpa/file-manager)](https://hub.docker.com/r/wzpa/file-manager)
[![Stars](https://img.shields.io/github/stars/Wzp-2008/FileManager)](https://github.com/Wzp-2008/FileManager/stargazers)
[![Last Commit](https://img.shields.io/github/last-commit/Wzp-2008/FileManager)](https://github.com/Wzp-2008/FileManager/commits)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/)

[English](README.md)

## ✨ 功能特性

- **分块断点续传** - 大文件分块上传，中断后可继续
- **文件夹组织** - 嵌套文件夹，拖拽操作
- **JWT 用户认证** - 安全的账号体系
- **点对点传输** - 通过 WebRTC 直连传输文件（WebSocket 信令）
- **PostgreSQL** - 主要支持的数据库（MySQL 仍可用但不推荐，详见下方说明）
- **一行命令 Docker 部署**

> [!NOTE]
> **推荐使用 PostgreSQL。** MySQL 仍可使用，但后续版本很可能不再维护。优先选择 PostgreSQL 是因其更优异的性能、对标准 SQL 语法的更好兼容，以及更坚定的开源立场。

## 🚀 快速开始

为了方便部署，提供了两个 compose 文件。先复制环境变量模板，再按你的场景选择：

```bash
cp .env.example .env
# 编辑 .env：设置 HMAC_KEY、数据库密码等
```

### 方案 A - 自带数据库（推荐）

自带 PostgreSQL 和 Redis，无需额外安装：

```bash
docker compose up -d
```

### 方案 B - 接入已有数据库

如果你已有 PostgreSQL 和 Redis，使用精简版 compose。在 `.env` 中将 `POSTGRES_HOST` / `REDIS_HOST` 指向你的数据库（数据库在宿主机上时用 `host.docker.internal`）：

```bash
docker compose -f docker-compose.external-db.yaml up -d
```

服务启动后访问 `http://localhost:${PORT:-23456}`。

## ⚙️ 配置说明

所有运行时配置集中在 `.env`，关键变量：

| 变量           | 说明                                          | 默认值                      |
|--------------|---------------------------------------------|--------------------------|
| `IMAGE`      | 拉取的 Docker 镜像                               | `wzpa/file-manager:2.0.0` |
| `PORT`       | 宿主机端口（容器内固定 8080）                           | `23456`                  |
| `POSTGRES_*` | PostgreSQL 连接 / 初始化                         | 见 `.env.example`         |
| `REDIS_*`    | Redis 连接                                    | 见 `.env.example`         |
| `HMAC_KEY`   | JWT 签名密钥 - **必须修改**，不要用 `RANDOM`（重启会导致用户掉线） | -                        |

完整列表见 [`.env.example`](.env.example)。

## 🛠️ 开发者看这里

- [API 文档](https://f4lomslwi6.apifox.cn)
- 后端：Java 17 + Spring Boot，`./gradlew bootJar` 构建
- 前端位于 `v2-web` 分支（使用 [Bun](https://bun.sh) 构建）

## 🤝 参与贡献

欢迎提交 PR，详见 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 📄 开源协议

[MIT](LICENSE)
