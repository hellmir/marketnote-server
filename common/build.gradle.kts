val queryDslVersion = "5.1.0"

plugins {
    java
    `java-library`
    `java-test-fixtures`
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

    // AOP — @Async/@Scheduled 메서드 entry/exit/exception DEBUG 로깅 aspect 활성화
    // 컨슈머 모듈은 런타임 AOP 프록시만 활용하고 aspectjweaver/spring-aop 타입을 컴파일타임 직접 참조하지 않으므로 implementation scope
    implementation("org.springframework.boot:spring-boot-starter-aop")

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

    // JSON 로그 인코더 (CloudWatch JSON 포맷)
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")

    // kafka
    implementation("org.springframework.kafka:spring-kafka")

    // micrometer (Kafka Consumer 메트릭 수집)
    implementation("io.micrometer:micrometer-core")

    // Micrometer Tracing — W3C traceparent 분산 추적 (OpenTelemetry bridge)
    // Spring Boot 3.5 자동 설정으로 MDC traceId/spanId 키 자동 put + RestClient/WebClient/RestTemplate traceparent 헤더 부착
    // OTLP exporter로 Tempo 등 분산 추적 백엔드 전송 (management.otlp.tracing.endpoint 미설정 시 no-op)
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")

    // ShedLock (분산 스케줄러 락 — desired ≥ 2 환경에서 @Scheduled 중복 실행 방지)
    // 7개 서비스 어댑터에서 @SchedulerLock 어노테이션 직접 사용하므로 api scope로 전파
    api("net.javacrumbs.shedlock:shedlock-spring:5.16.0")
    api("net.javacrumbs.shedlock:shedlock-provider-redis-spring:5.16.0")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")

    // Spring Boot Tracing 자동 구성 라운드트립 테스트(TracingAutoConfigurationMdcTest) 한정
    // BraveAutoConfiguration / MicrometerTracingAutoConfiguration이 actuator-autoconfigure에 위치
    testImplementation("org.springframework.boot:spring-boot-starter-actuator")

    // ArchUnit — 도메인 예외 네이밍/상속 매핑 및 생성자 규약 강제
    testFixturesApi("com.tngtech.archunit:archunit-junit5:1.3.0")
    testImplementation(testFixtures(project(":common")))
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<Test>("test") {
    useJUnitPlatform {
        excludeTags("broker-failure")
    }
}

tasks.register<Test>("brokerFailureTest") {
    description = "KafkaBrokerFailureScenarioTest를 독립 JVM에서 실행한다"
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    shouldRunAfter("test")
    useJUnitPlatform {
        includeTags("broker-failure")
    }
}

// brokerFailureTest는 수동 실행 전용 (./gradlew :common:brokerFailureTest)
// EmbeddedKafka 브로커 장애 시뮬레이션은 타이밍 의존적이므로 일반 빌드에서 제외

tasks.named("bootJar") {
    enabled = false
}

tasks.register("prepareKotlinBuildScriptModel") {
    doLast {
        println("Dummy task for prepareKotlinBuildScriptModel executed")
    }
}
