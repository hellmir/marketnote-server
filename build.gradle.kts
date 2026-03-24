plugins {
    base
    jacoco
}

repositories {
    mavenCentral()
}

jacoco {
    toolVersion = "0.8.12"
}

// 모든 서브프로젝트에 JaCoCo 플러그인 적용
subprojects {
    apply(plugin = "jacoco")
    configure<JacocoPluginExtension> {
        toolVersion = "0.8.12"
    }
}

// JaCoCo 집계 리포트 (build/reports/test/html/index.html)
// 이미 빌드/테스트된 결과물을 파일 시스템에서 직접 수집
tasks.register<JacocoReport>("jacocoAggregatedReport") {
    group = "verification"
    description = "Generates aggregated JaCoCo HTML coverage report for all subprojects"
    mustRunAfter("testAggregatedReport")

    // exec 파일 수집
    executionData.from(fileTree(rootDir) { include("**/build/jacoco/test.exec") })

    // 이미 컴파일된 클래스 파일 수집
    val classDirs = files()
    val srcDirs = files()

    subprojects.forEach { subproject ->
        val excludes = mutableListOf("**/Q*.class") // QueryDSL 생성 클래스 제외
        // -application과 -adapters에 동일 FQCN 클래스(CommonConfig 등) 중복 존재 → -application에서 제외
        if (subproject.name.endsWith("-application")) {
            excludes.add("**/configuration/CommonConfig.class")
        }
        classDirs.from(fileTree("${subproject.projectDir}/build/classes/java/main") {
            exclude(excludes)
        })
        srcDirs.from("${subproject.projectDir}/src/main/java")
    }

    classDirectories.from(classDirs)
    sourceDirectories.from(srcDirs)

    reports {
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/test/html"))
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/test/html/jacoco.xml"))
    }
}

// Gradle 테스트 결과 집계 리포트 (build/reports/test/index.html)
tasks.register<TestReport>("testAggregatedReport") {
    group = "verification"
    description = "Generates aggregated Gradle test report for all subprojects"
    destinationDirectory.set(layout.buildDirectory.dir("reports/test"))

    // 이미 실행된 테스트 바이너리 결과 수집
    subprojects.forEach { subproject ->
        val binaryDir = file("${subproject.projectDir}/build/test-results/test/binary")
        if (binaryDir.exists()) {
            testResults.from(binaryDir)
        }
    }
}
