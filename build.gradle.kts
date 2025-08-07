import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.github.gradle.node.npm.task.NpmTask
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    id("com.bmuschko.docker-remote-api") version "9.4.0"
    id("com.github.ben-manes.versions") version "0.52.0"
    id("com.github.node-gradle.node") version "7.1.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "13.0.0"
    id("org.springframework.boot") version "3.5.4"
    jacoco
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.spring") version "2.2.0"
}

group = "com.hamza"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
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
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    testImplementation("io.projectreactor:reactor-test")
    // testImplementation("io.projectreactor.tools:blockhound-junit-platform:1.0.13.RELEASE")
    testImplementation("org.htmlunit:htmlunit")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.isFork = true
    options.isIncremental = true
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs(
        "--enable-native-access=ALL-UNNAMED",
        "-XX:+EnableDynamicAgentLoading",
    )
//    if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_13)) {
//        jvmArgs("-XX:+AllowRedefinitionToAddDeleteMethods")
//    }
    finalizedBy(tasks.jacocoTestReport)
    configure<JacocoTaskExtension> {
        excludes = listOf("org/htmlunit/**", "jdk.internal.*")
        isIncludeNoLocationClasses = true
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    maxParallelForks = 2
//    forkEvery = 100
    reports {
        html.required = false
        junitXml.required = false
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        csv.required = false
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
        xml.required = false
    }
}

configure<KtlintExtension> {
    android.set(false)
    coloredOutput.set(true)
    debug.set(false)
    verbose.set(false)
    version.set("1.7.1")
}

node {
    download = true
}

tasks.processResources {
    finalizedBy("npm_run_build")
}

tasks.register("npm_run_build", NpmTask::class) {
    val isDev = project.hasProperty("mode") && project.property("mode") == "development"
    inputs.files(
        fileTree("$projectDir/src/main/resources/static/ts"),
        fileTree("$projectDir/src/main/resources/static/scss"),
    )
    outputs.files(fileTree("$projectDir/src/main/resources/static/dist"))
    args = listOf("run", if (isDev) "build:dev" else "build")
}

tasks.jar {
    enabled = false
}

val prepareDockerContext by tasks.registering(Sync::class) {
    dependsOn(tasks.bootJar)
    project.tasks.findByName("npm_run_build")?.let {
        dependsOn(it)
    }
    inputs.files(
        fileTree("src") {
            exclude("main/resources/static/dist/**")
        },
    )
    outputs.dir(layout.buildDirectory.dir("docker-context"))
    from("Dockerfile")
    from("build/libs") {
        into("build/libs")
    }
    into(layout.buildDirectory.dir("docker-context"))
}

tasks.register<DockerBuildImage>("buildLocalDockerImage") {
    dependsOn(prepareDockerContext)
    inputDir.set(layout.buildDirectory.dir("docker-context"))
    images.set(listOf("spring-webpack-template:latest"))
    outputs.cacheIf { true }
}

tasks.check {
    finalizedBy("buildLocalDockerImage")
}
