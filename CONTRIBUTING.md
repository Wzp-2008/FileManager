# Contributing

Thanks for your interest in File Manager! Issues and Pull Requests are welcome.

[简体中文](CONTRIBUTING_CN.md)

## Development Environment

- **JDK 17**
- **Gradle** (wrapper included, no global install needed)
- **PostgreSQL / Redis** (run locally, or use `docker-compose.yaml` to spin up a stack)
- Frontend lives on the `v2-web` branch, built with [Bun](https://bun.sh)

## Run Locally

```bash
# 1. Prepare config
cp src/main/resources/example-application.yaml src/main/resources/application.yaml
#    Edit DB credentials, Redis, HMAC_KEY, etc. as needed

# 2. Start PostgreSQL / Redis (see docker-compose.yaml)

# 3. Build and run
./gradlew bootRun
```

## Commit Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/), e.g.:

- `feat: support folder drag-and-drop upload`
- `fix: throw on null chunks`
- `docs: add deployment guide`
- `ci: tweak release pipeline`
- `refactor: rework auth logic`

## Pull Request Flow

1. Fork the repo and branch off `master`: `feat/xxx`, `fix/xxx`
2. Make sure it builds locally: `./gradlew build`
3. Open a PR with a clear description of what and why
4. Address review feedback and iterate

## Code Style

- Follow the existing style (naming, comment density, etc.)
- Add necessary comments for new features
- Keep the API document in sync when public APIs change

## Reporting Bugs

Please open an Issue with:

- Steps to reproduce
- Expected vs. actual behavior
- Logs / screenshots (if any)
