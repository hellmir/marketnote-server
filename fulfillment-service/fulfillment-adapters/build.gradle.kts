val serviceName = project.path.removePrefix(":").substringBefore(":")
val javaVersion = 21
val lombokVersion = "1.18.34"
val dotenvVersion = "3.0.0"
val h2Version = "2.2.224"
val projectEncoding = "UTF-8"
val queryDslVersion = "5.1.0"
val mapstructVersion = "1.5.5.Final"

plugins {
    java
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jetbrains.kotlin.jvm") version "1.7.22" apply false
}

group = "com.personal.marketnote.fulfillment.adapters"
version = "1.0.0"
description = "fulfillment service adapters"

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
    // module
    implementation(project(":common"))
    implementation(project(":fulfillment-service:fulfillment-application"))
    implementation(project(":fulfillment-service:fulfillment-domain"))

    // 🔹 Spring Boot 관련 의존성
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // JPA (데이터베이스 ORM)
    implementation("org.springframework.boot:spring-boot-starter-web") // Spring MVC (REST API 개발)
    implementation("org.springframework.boot:spring-boot-starter-validation") // Spring Validation
    implementation("org.springframework.boot:spring-boot-starter-security") // Spring Security
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server") // OAuth 2.0 Resource server

    // Spring Kafka
    implementation("org.springframework.kafka:spring-kafka")

    //querydsl 설정
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api:3.1.0")

    implementation("com.querydsl:querydsl-jpa:$queryDslVersion:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:$queryDslVersion:jakarta")

    // 🔹 Lombok 설정 (코드 자동 생성 도구)
    compileOnly("org.projectlombok:lombok:$lombokVersion") // 빌드 타임에만 필요한 라이브러리
    annotationProcessor("org.projectlombok:lombok:$lombokVersion") // 애너테이션 프로세서 활성화
    testCompileOnly("org.projectlombok:lombok:$lombokVersion") // 빌드 타임에만 필요한 라이브러리
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion") // 애너테이션 프로세서 활성화

    // security
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // JSON parser
    implementation("org.json:json:20240303")

    // Jackson Hibernate Module (Jakarta, Boot 3.x/Hibernate 6 호환)
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-hibernate5-jakarta")

    // MapStruct
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    // Lombok과 MapStruct 통합 (Lombok이 먼저 처리되도록)
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // 🔹 테스트 관련 의존성
    testImplementation("org.springframework.boot:spring-boot-starter-test") // 테스트를 위한 기본 라이브러리
    testImplementation("org.springframework.security:spring-security-test") // Spring Security 테스트 지원
    testRuntimeOnly("org.junit.platform:junit-platform-launcher") // JUnit 테스트 런처
    testImplementation("org.awaitility:awaitility:4.2.0") // 비동기,스케줄링 테스트 지원
    testImplementation("org.springframework.kafka:spring-kafka-test")

    // 🔹 추가 라이브러리
    // dotenv
    implementation("io.github.cdimascio:dotenv-java:$dotenvVersion")

    // H2 데이터베이스 (테스트용)
    runtimeOnly("com.h2database:h2:$h2Version")

    // PostgreSQL 드라이버
    runtimeOnly("org.postgresql:postgresql:42.7.4")

    // Spring Boot Actuator(Prometheus Monitoring Query)
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Swagger API 문서 생성
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")

    // HNSW 라이브러리
    implementation("com.github.jelmerk:hnswlib-core:1.2.1")

    // MIME 타입 체크를 위한 Apache Tika
    implementation("org.apache.tika:tika-core:3.1.0")

    // Prometheus
    implementation("io.micrometer:micrometer-registry-prometheus")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.3")
}

// ✅ 테스트 실행 시 JUnit 5 플랫폼 사용 설정
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test")
}

// ✅ UTF-8 인코딩 설정 (한글 깨짐 방지)
tasks.withType<JavaCompile>().configureEach {
    options.encoding = projectEncoding
}

tasks.register("printProjectName") {
    val name = serviceName
    doLast {
        println(name)
    }
}

tasks.register("printProjectVersion") {
    val ver = version.toString()
    doLast {
        println(ver)
    }
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveBaseName.set(serviceName)
    enabled = true
}
tasks.named<Jar>("jar") {
    enabled = false
}

springBoot {
    mainClass.set("com.personal.marketnote.fulfillment.FulfillmentApplication")
    buildInfo()
}

tasks.register("prepareKotlinBuildScriptModel") {
    doLast {
        println("Dummy task for prepareKotlinBuildScriptModel executed")
    }
}
