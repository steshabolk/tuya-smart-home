import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
	id("org.springframework.boot") version "3.1.4"
	id("io.spring.dependency-management") version "1.1.3"
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
	kotlin("plugin.jpa") version "1.8.22"
}

group = "ru.handh"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
	maven { url = uri("https://maven-other.tuya.com/repository/maven-public/") }
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	implementation("io.github.microutils:kotlin-logging:3.0.5")

	implementation("org.liquibase:liquibase-core")
	runtimeOnly("org.postgresql:postgresql")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("com.ninja-squad:springmockk:4.0.2")
	testImplementation("org.testcontainers:postgresql:1.18.3")

	implementation("com.tuya:tuya-spring-boot-starter:1.3.2")

	implementation("org.springframework.kafka:spring-kafka")

	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
}

allOpen {
	annotations("javax.persistence.Entity", "javax.persistence.MappedSuperclass", "javax.persistence.Embeddable")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

sourceSets.main {
	java.srcDirs("src/main/kotlin")
}

tasks.withType<BootRun> {
	jvmArgs(
		"--add-opens=java.base/java.lang=ALL-UNNAMED",
		"--add-opens=java.management/sun.management=ALL-UNNAMED",
		"--add-opens=java.base/sun.net=ALL-UNNAMED",
	)
}

tasks.withType<Test> {
	useJUnitPlatform()
	jvmArgs(
		"--add-opens", "java.base/java.lang.reflect=ALL-UNNAMED"
	)
}
