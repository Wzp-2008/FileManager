plugins {
	java
	id("org.springframework.boot") version "3.3.4"
	id("io.spring.dependency-management") version "1.1.6"
	id("org.graalvm.buildtools.native") version "0.10.3"
	id("org.asciidoctor.jvm.convert") version "3.3.2"
}

group = "cn.wzpmc"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

extra["snippetsDir"] = file("build/generated-snippets")
extra["springShellVersion"] = "3.3.3"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-web"){
		// remove jackson
		exclude("org.springframework.book", "spring-boot-starter-json")
	}
	implementation("org.springframework.shell:spring-shell-starter")
	// https://mvnrepository.com/artifact/com.mybatis-flex/mybatis-flex-spring-boot3-starter
	implementation("com.mybatis-flex:mybatis-flex-spring-boot3-starter:1.9.7")
	// https://mvnrepository.com/artifact/com.alibaba.fastjson2/fastjson2
	implementation("com.alibaba.fastjson2:fastjson2:2.0.53")
	// https://mvnrepository.com/artifact/com.alibaba.fastjson2/fastjson2-extension
	implementation("com.alibaba.fastjson2:fastjson2-extension:2.0.53")
	// https://mvnrepository.com/artifact/com.alibaba.fastjson2/fastjson2-extension-spring6
	implementation("com.alibaba.fastjson2:fastjson2-extension-spring6:2.0.53")
	// https://mvnrepository.com/artifact/com.alibaba/druid-spring-boot-starter
	implementation("com.alibaba:druid-spring-boot-starter:1.2.23")
	// https://mvnrepository.com/artifact/com.auth0/java-jwt
	implementation("com.auth0:java-jwt:4.4.0")
	// https://mvnrepository.com/artifact/commons-codec/commons-codec
	implementation("commons-codec:commons-codec:1.17.1")
	// https://mvnrepository.com/artifact/org.apache.tika/tika-core
	implementation("org.apache.tika:tika-core:3.0.0-BETA2")
	// https://mvnrepository.com/artifact/com.mysql/mysql-connector-j
	implementation("com.mysql:mysql-connector-j:9.0.0")
	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
	testImplementation("org.springframework.shell:spring-shell-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.shell:spring-shell-dependencies:${property("springShellVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.test {
	outputs.dir(project.extra["snippetsDir"]!!)
}

tasks.asciidoctor {
	inputs.dir(project.extra["snippetsDir"]!!)
	dependsOn(tasks.test)
}