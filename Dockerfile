FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 复制构建产物（fat jar），自动跳过 *-plain.jar
COPY build/libs/FileManager-*.jar /tmp/
RUN mv "$(ls /tmp/FileManager-*.jar | grep -v -- '-plain' | head -1)" app.jar \
    && rm -f /tmp/FileManager-*.jar

# 复制前端静态资源（由 CI 构建到 frontend-src/dist，Spring Boot 从 ./static/ 提供）
COPY frontend-src/dist ./static

# 以非 root 用户运行，降低容器逃逸风险
RUN addgroup -S app && adduser -S app -G app \
    && chown -R app:app /app
USER app

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
