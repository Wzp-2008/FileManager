# 参与贡献

感谢你关注 File Manager！欢迎提交 Issue 和 Pull Request。

[English](CONTRIBUTING.md)

## 开发环境

- **JDK 17**
- **Gradle**（项目自带 `gradlew`，无需全局安装）
- **PostgreSQL / Redis**（可本地运行，或参考 `docker-compose.yaml` 一键起一套）
- 前端位于 `v2-web` 分支，使用 [Bun](https://bun.sh) 构建

## 本地运行

```bash
# 1. 准备配置
cp src/main/resources/example-application.yaml src/main/resources/application.yaml
#    按需修改数据库、Redis、HMAC_KEY 等

# 2. 启动 PostgreSQL / Redis（可参考 docker-compose.yaml）

# 3. 构建并运行
./gradlew bootRun
```

## 提交规范

使用 [Conventional Commits](https://www.conventionalcommits.org/)，例如：

- `feat: 支持文件夹拖拽上传`
- `fix: 修复分块为空时的异常`
- `docs: 补充部署说明`
- `ci: 调整发布流程`
- `refactor: 重构认证逻辑`

## PR 流程

1. Fork 仓库并从 `master` 创建分支：`feat/xxx`、`fix/xxx`
2. 确保本地能正常构建：`./gradlew build`
3. 提交 PR，描述清楚改动内容与动机
4. 等待 review，根据反馈迭代

## 代码风格

- 遵循现有代码风格（命名、注释密度等）
- 新增功能请补充必要注释
- 公共 API 变动需同步更新 API 文档

## 报告 Bug

请通过 Issue 提交，附上：

- 复现步骤
- 期望行为与实际行为
- 日志 / 截图（如有）
