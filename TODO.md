# 项目待办与未来规划

> 最近完成：Docker 部署链路（Dockerfile / 双 compose / .env / .dockerignore）、双语 README + badge、CONTRIBUTING、Docker Hub 发布 CI、默认数据库切换为 PostgreSQL。

## 文档

1. README 补充截图（界面 / 上传流程 / P2P 传输演示）
2. 在 README 中说明 `application.yaml`（固定基础配置：数据源类型、mybatis-flex type-handlers、multipart resolve-lazily）与 `example-application.yaml`（用户需填写：DB 账号密码、Redis、hmac-key、save-path）的分层关系
3. 确定注释 / 日志 / 错误消息的语言基调（纯中文），全项目统一
4. 补全 `FileService` 的方法级 Javadoc，与 Controller 注释密度对齐

## 开源治理

5. 添加 Issue / PR 模板（`.github/ISSUE_TEMPLATE/`、`PULL_REQUEST_TEMPLATE.md`）
6. Docker Hub 仓库页补充 README 与概要描述

## CI/CD

7. CI 增加测试步骤（当前仅 `bootJar`）
8. CI 增加 lint（checkstyle / spotless）
9. CI 增加安全扫描（CodeQL / Trivy）
10. 验证 `docker.yaml` 多架构（amd64 + arm64）在 Gitea act runner 上的实际运行；若 QEMU 不可用则降级为单架构

## 数据库

11. 后续逐步移除 MySQL 支持：删除 `databaseId="mysql"` 方言 SQL、`mysql-connector-j` 依赖、`DatabaseConfiguration` 中的 mysql 族归类，使 PostgreSQL 成为唯一支持数据库

## 测试

12. 补充业务测试：上传 / 下载 / 分块 / 秒传指纹 / JWT 鉴权 / 文件移动 / 文件夹管理等核心逻辑

## 功能规划

13. 添加分布式功能，允许下载时从多个源（或类似 CDN）下载以解决家庭宽带速度慢的问题
