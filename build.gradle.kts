plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.spring") version "2.1.10"
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.autonomousapps.dependency-analysis") version "2.10.1"
}

group = "com.intezya"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot dependencies
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    // Kotlin dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
    implementation(kotlin("stdlib-jdk8"))

    // Jackson dependencies
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // JWT dependencies
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Liquibase and BouncyCastle dependencies
    runtimeOnly("org.liquibase:liquibase-core:4.23.0")
    runtimeOnly("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")

    // Security & Authentication dependencies
    implementation("de.mkammerer:argon2-jvm:2.11")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.security:spring-security-test")

    // Database dependencies
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("com.zaxxer:HikariCP:5.1.0")
    testImplementation("org.testcontainers:postgresql:1.19.5")

    // Swagger/OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")

    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter:1.19.5")
    testRuntimeOnly("org.testcontainers:testcontainers:1.19.5")
    testImplementation("io.github.serpro69:kotlin-faker:1.16.0")
    testImplementation("io.rest-assured:rest-assured:5.3.0")
    testImplementation("io.rest-assured:kotlin-extensions:5.3.0")
    testImplementation("io.rest-assured:json-path:5.3.0")
    testImplementation("io.rest-assured:xml-path:5.3.0")

    // Lombok
    runtimeOnly("org.projectlombok:lombok")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
