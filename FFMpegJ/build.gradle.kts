plugins {
    java
}

group = "cn.wzpmc.filemanager.ffmpeg"
version = "0.0.1-SNAPSHOT"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}
repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    compileOnly("org.projectlombok:lombok:1.18.26")
    annotationProcessor("org.projectlombok:lombok:1.18.26")
    // https://mvnrepository.com/artifact/com.alibaba.fastjson2/fastjson2
    implementation("com.alibaba.fastjson2:fastjson2:2.0.26")
    implementation("org.apache.tika:tika-core:2.7.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
