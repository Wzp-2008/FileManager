# 项目待办与未来规划

## 文档

1. 补全 `FileService` 的方法级 Javadoc，与 Controller 注释密度对齐

## 开源治理

2. Docker Hub 仓库页补充 README 与概要描述

## CI/CD

3. CI 增加测试步骤（当前仅 `bootJar`）
4. CI 增加 lint（checkstyle / spotless）
5. CI 增加安全扫描（CodeQL / Trivy）

## 测试

6. 补充业务测试：上传 / 下载 / 分块 / 秒传指纹 / JWT 鉴权 / 文件移动 / 文件夹管理等核心逻辑

## 功能规划

7. 添加分布式功能，允许下载时从多个源（或类似 CDN）下载以解决家庭宽带速度慢的问题
