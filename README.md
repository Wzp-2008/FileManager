# File Manager

> A self-hosted file management platform with resumable uploads and peer-to-peer sharing.

[![License](https://img.shields.io/github/license/Wzp-2008/FileManager)](LICENSE)
[![Release](https://img.shields.io/github/v/release/Wzp-2008/FileManager)](https://github.com/Wzp-2008/FileManager/releases)
[![Docker Pulls](https://img.shields.io/docker/pulls/wzpa/file-manager)](https://hub.docker.com/r/wzpa/file-manager)
[![Stars](https://img.shields.io/github/stars/Wzp-2008/FileManager)](https://github.com/Wzp-2008/FileManager/stargazers)
[![Last Commit](https://img.shields.io/github/last-commit/Wzp-2008/FileManager)](https://github.com/Wzp-2008/FileManager/commits)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/)

[简体中文](README_CN.md)

## ✨ Features

- **Resumable chunked uploads** - large files are split into chunks and can resume on interruption
- **Folder organization** - nested folders with drag-and-drop
- **JWT authentication** - secure user accounts
- **Peer-to-peer transfer** - direct device-to-device transfer via WebRTC (signaled over WebSocket)
- **PostgreSQL** - primary supported database (MySQL still works but is no longer recommended, see note below)
- **One-command Docker deployment**

> [!NOTE]
> **PostgreSQL is recommended.** MySQL remains usable but will likely be deprecated in future releases. PostgreSQL is preferred for its superior performance, better compliance with standard SQL syntax, and stronger commitment to open source.

## 🚀 Quick Start (Docker)

Two compose files are provided. Copy the environment template first, then pick the one that fits your setup:

```bash
cp .env.example .env
# Edit .env: set HMAC_KEY, database passwords, etc.
```

### Option A - All-in-one (recommended)

Bundles PostgreSQL and Redis alongside the app. Nothing else to install:

```bash
docker compose up -d
```

### Option B - Use your existing database

If you already have PostgreSQL and Redis running, use the slim compose file. Set `POSTGRES_HOST` / `REDIS_HOST` in `.env` to point at your databases (use `host.docker.internal` when they run on the host machine):

```bash
docker compose -f docker-compose.external-db.yaml up -d
```

The service is available at `http://localhost:${PORT:-23456}`.

## ⚙️ Configuration

All runtime config lives in `.env`. Key variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `IMAGE` | Docker image to pull | `wzpa/file-manager:2.0.0` |
| `PORT` | Host port mapped to container's 8080 | `23456` |
| `POSTGRES_*` | PostgreSQL connection / initialization | see `.env.example` |
| `REDIS_*` | Redis connection | see `.env.example` |
| `HMAC_KEY` | JWT signing key - **must be changed**; do not use `RANDOM` (sessions break on restart) | - |

See [`.env.example`](.env.example) for the full list.

## 🛠️ For Developers

- [API Document](https://f4lomslwi6.apifox.cn)
- Backend: Java 17 + Spring Boot, build with `./gradlew bootJar`
- Frontend lives on the `v2-web` branch (built with [Bun](https://bun.sh))

### Configuration Layout

- `application.yaml` — built-in baseline config (datasource type, mybatis-flex type-handlers, multipart resolve-lazily, application name); generally no need to touch
- `example-application.yaml` — local-dev template with user-filled values (DB credentials, Redis, hmac-key, save-path). Copy it to `application.yaml` and edit for local runs

See [CONTRIBUTING.md](CONTRIBUTING.md) for the local run steps.

## 🤝 Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## 📋 Roadmap

See [TODO.md](TODO.md) for the project roadmap and future plans.

## 📄 License

[MIT](LICENSE)
