import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    id("org.openapi.generator") version "7.1.0"
    kotlin("jvm") version "1.8.22"
    kotlin("plugin.spring") version "1.8.22"
}

group = "ru.handh"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("io.github.microutils:kotlin-logging:3.0.5")

    implementation("com.nimbusds:nimbus-jose-jwt:9.37")

    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

val openapiDir = "$projectDir/src/main/resources/openapi"
val apiDocs = "api-docs.yaml"
val projectPath = "ru.handh"
val dirs = mapOf("openapi" to openapiDir)
val apiDocsNames = fileTree(dirs["openapi"]!!)
    .filter { it.extension == "yaml" }
    .map { it.name.replace("-$apiDocs", "") }

val generateOpenApiTasks = apiDocsNames.map { createOpenApiGenerateTask(it) }

tasks.withType<KotlinCompile> {
    dependsOn(generateOpenApiTasks)
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs(
        "--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED"
    )
}

sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated/src/main/kotlin").get().asFile.absoluteFile)
        }
    }
}

fun createOpenApiGenerateTask(serviceName: String) =
    tasks.register<GenerateTask>("openApiGenerate${serviceName.replaceFirstChar(Char::titlecase)}Client") {
        group = "openapi tools"
        generatorName.set("kotlin")
        input = project.file("$openapiDir/$serviceName-$apiDocs").path
        outputDir.set(layout.buildDirectory.dir("generated").get().asFile.path)
        apiPackage.set("$projectPath.$serviceName.client.api")
        modelPackage.set("$projectPath.$serviceName.client.model")
        modelNameSuffix.set("Gen")
        templateDir.set("$openapiDir/templates")
        configOptions.set(
            mapOf(
                "dateLibrary" to "java8-localdatetime",
                "useTags" to "true",
                "enumPropertyNaming" to "UPPERCASE",
                "serializationLibrary" to "jackson",
                "useCoroutines" to "true",
                "useSpringBoot3" to "true",
                "interfaceOnly" to "true",
                "skipDefaultInterface" to "true"
            )
        )
        additionalProperties.set(
            mapOf(
                "removeEnumValuePrefix" to "false"
            )
        )
    }
