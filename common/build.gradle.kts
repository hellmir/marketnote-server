val queryDslVersion = "5.1.0"

plugins {
    java
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jetbrains.kotlin.jvm") version "1.7.22" apply false
}

group = "com.personal.marketnote.common"
version = "1.0.0"
description = "common"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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

dependencies {
    // spring boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    //querydsl 설정
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")

    implementation("com.querydsl:querydsl-jpa:$queryDslVersion:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:$queryDslVersion:jakarta")

    // lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // data
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // security
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // apache tika
    implementation("org.apache.tika:tika-core:3.1.0")

    // 스웨거 API 문서 생성
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")

    // SMTP 서버 통신 관련
    implementation("org.springframework:spring-context-support")

    // UUID v7
    implementation("com.github.f4b6a3:uuid-creator:5.3.0")

    // kafka
    implementation("org.springframework.kafka:spring-kafka")

    // micrometer (Kafka Consumer 메트릭 수집)
    implementation("io.micrometer:micrometer-core")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named("bootJar") {
    enabled = false
}

tasks.register("prepareKotlinBuildScriptModel") {
    doLast {
        println("Dummy task for prepareKotlinBuildScriptModel executed")
    }
}
