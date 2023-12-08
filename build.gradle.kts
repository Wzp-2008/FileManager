plugins {
    java
    id("org.springframework.boot") version "3.0.5"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.graalvm.buildtools.native") version "0.9.20"
}

group = "cn.wzpmc.filemanager"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web"){
        exclude("org.springframework.book", "spring-boot-starter-json")
    }
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.1")
    // https://mvnrepository.com/artifact/com.auth0/java-jwt
    implementation("com.auth0:java-jwt:4.3.0")
    // https://mvnrepository.com/artifact/commons-codec/commons-codec
    implementation("commons-codec:commons-codec:1.15")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    // https://mvnrepository.com/artifact/com.alibaba.fastjson2/fastjson2
    implementation("com.alibaba.fastjson2:fastjson2:2.0.28")
    // https://mvnrepository.com/artifact/com.alibaba.fastjson2/fastjson2-extension
    implementation("com.alibaba.fastjson2:fastjson2-extension:2.0.28")
    implementation("com.alibaba.fastjson2:fastjson2-extension-spring6:2.0.28")
    compileOnly("org.projectlombok:lombok:1.18.26")
    runtimeOnly("com.mysql:mysql-connector-j")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
