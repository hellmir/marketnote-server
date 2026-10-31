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

subprojects {
    apply(plugin = "jacoco")
    configure<JacocoPluginExtension> {
        toolVersion = "0.8.12"
    }

    tasks.withType<Test>().configureEach {
        maxHeapSize = "512m"
        minHeapSize = "256m"
        forkEvery = 20
    }
}

tasks.register<JacocoReport>("jacocoAggregatedReport") {
    group = "verification"
    description = "Generates aggregated JaCoCo HTML coverage report for all subprojects"
    mustRunAfter("testAggregatedReport")

    executionData.from(fileTree(rootDir) { include("**/build/jacoco/test.exec") })

    val classDirs = files()
    val srcDirs = files()

    subprojects.forEach { subproject ->
        val excludes = mutableListOf("**/Q*.class")
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

tasks.register<TestReport>("testAggregatedReport") {
    group = "verification"
    description = "Generates aggregated Gradle test report for all subprojects"
    destinationDirectory.set(layout.buildDirectory.dir("reports/test"))

    subprojects.forEach { subproject ->
        val binaryDir = file("${subproject.projectDir}/build/test-results/test/binary")
        if (binaryDir.exists()) {
            testResults.from(binaryDir)
        }
    }
}
