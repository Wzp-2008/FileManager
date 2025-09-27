plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.graalvm.buildtools.native") version "0.10.3"
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
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web"){
        exclude("org.springframework.book", "spring-boot-starter-json")
    }
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3") {
        exclude("org.mybatis", "mybatis")
    }
    // https://mvnrepository.com/artifact/org.mybatis/mybatis
    implementation("org.mybatis:mybatis:3.5.16")
/*    compileOnly("org.mybatis.spring.native:mybatis-spring-native-core:0.1.0-SNAPSHOT")
    compileOnly("org.mybatis.spring.native:mybatis-spring-native-extensions:0.1.0-SNAPSHOT")*/
    // https://mvnrepository.com/artifact/com.auth0/java-jwt
    implementation("com.auth0:java-jwt:4.3.0")
    // https://mvnrepository.com/artifact/commons-codec/commons-codec
    implementation("commons-codec:commons-codec:1.15")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    // https://mvnrepository.com/artifact/com.alibaba.fastjson2/fastjson2
    implementation("com.alibaba.fastjson2:fastjson2:2.0.53")

    implementation("com.alibaba.fastjson2:fastjson2-extension-spring6:2.0.53")
    compileOnly("org.projectlombok:lombok:1.18.34")
    runtimeOnly("com.mysql:mysql-connector-j")
    annotationProcessor("org.projectlombok:lombok:1.18.34")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}