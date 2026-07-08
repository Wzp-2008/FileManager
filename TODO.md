# 项目待办与未来规划

## 文档

1. 补全 README.md：特性介绍、截图、技术栈、快速开始、环境要求（JDK17 / MySQL或PostgreSQL / Redis）、部署方式、配置项说明、API文档入口、开发指南
2. 在 README 中说明 `application.yaml`（固定基础配置：数据源类型、mybatis-flex type-handlers、multipart resolve-lazily、application name）与 `example-application.yaml`（用户需手动填写的配置：DB账号密码、Redis、hmac-key、save-path）的分层关系
3. 确定注释 / 日志 / 错误消息的语言基调（纯中文），全项目统一；README 出双语版本
4. 补全 `FileService` 的方法级 Javadoc，与 Controller 注释密度对齐（当前 commit `docs: add docs for code except services` 的遗留）

## 开源治理

5. 添加 `CONTRIBUTING.md`（贡献流程、代码规范、提 PR 约定）
6. 添加 Issue / PR 模板（`.github/ISSUE_TEMPLATE/`、`PULL_REQUEST_TEMPLATE.md`）
7. 打第一个 git tag / Release，脱离永久 `0.0.1-SNAPSHOT` 状态

## CI/CD

8. CI 增加测试步骤（当前仅 `bootJar`）
9. CI 增加 lint（checkstyle / spotless）
10. CI 增加安全扫描（CodeQL / Trivy）

## 测试

11. 补充业务测试：上传 / 下载 / 分块 / 秒传指纹 / JWT鉴权 / 文件移动 / 文件夹管理等核心逻辑

## 部署

12. 添加 `Dockerfile`
13. 添加 `docker-compose.yml`（含 MySQL/PostgreSQL + Redis + 应用）
14. CI 已产出 `FileManager-with-static` 组合包，需配合 Release 发布（避免 artifact 90 天过期）

## 代码质量与安全

15. `AuthorizationUtils.auth`（`:46`）中 `log.info("auth {} with token {}", ..., header)` 将完整 JWT 写入 info 日志，凭证泄露风险，应脱敏或降为 debug
16. `P2PService.channels`（`:40`）使用 `HashMap`，在 WebSocket 多连接并发访问下不安全（死循环/丢数据），应改为 `ConcurrentHashMap`（同文件 `ChannelWebSocketHandler` 内部已用 `ConcurrentHashMap`，风格不一致）
17. 移除生产代码中的 `assert` 空检查（JVM 默认 `-da` 禁用 assert 时会跳过、随后 NPE），当前剩余 4 处：`SimplePathResolver:37`、`FileService.saveFile:610`、`P2PService:70`、`AuthorizationArgumentResolver:36`（`UserService.login` 那处已在密码迁移重构中改为显式 null 检查）
18. `FileService.uploadChunk`（`:564`）中 `readAllBytes()` 将整个分块读入内存，大文件并发上传易 OOM，应改为流式写入临时文件
19. `FileService.downloadFile`（`:441-448`）的 Range 头解析无异常处理，`Long.parseLong` / 数组越界在格式异常时会 500，应 try-catch 并返回 416 Requested Range Not Satisfiable
20. `P2PService.handlePongMessage`（`:157-158`）中 `pingScheduler.remove(uuid)` 可能返回 null，后续 `remove.cancel(true)` 会 NPE，需判空
21. `FileService.simpleUpload`（`:226-230`）的 `catch (Exception e)` 吞掉异常后继续循环，多文件上传时部分失败仍可能返回成功，应记录失败并在最终结果中体现

## 功能规划

22. 添加分布式功能，允许下载时从多个源（或类似 CDN）下载以解决家庭宽带速度慢的问题
