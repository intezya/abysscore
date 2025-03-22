plugins {
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.spring") version "2.1.10"
    kotlin("plugin.jpa") version "2.1.10"
    id("com.autonomousapps.dependency-analysis") version "2.10.1"
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.8"
    id("com.github.ben-manes.versions") version "0.50.0"
}

group = "com.intezya"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
//    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
}

sourceSets {
    create("integrationTest") {
        kotlin {
            compileClasspath += main.get().output + test.get().output
            runtimeClasspath += main.get().output + test.get().output
        }
    }
}

repositories {
    mavenCentral()
}

val springBootVersion = "3.4.3"
val kotlinVersion = "2.1.10"
val testcontainersVersion = "1.19.5"
val mockKVersion = "1.13.8"
val jwtVersion = "0.11.5"
val bouncyCastleVersion = "1.77"

dependencies {
    // BOM dependencies
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion"))
    implementation(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))

    // Spring Boot core dependencies
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")

    // Spring Boot data dependencies
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-websocket")
    implementation("org.springframework.kafka:spring-kafka")

    // Database dependencies
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.liquibase:liquibase-core:4.23.0")

    // Kotlin dependencies
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

    // Jackson dependencies
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Security dependencies
    implementation("io.jsonwebtoken:jjwt-api:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jwtVersion")
    implementation("de.mkammerer:argon2-jvm:2.11")

    // Using newer BouncyCastle libraries
    implementation("org.bouncycastle:bcprov-jdk18on:$bouncyCastleVersion")
    implementation("org.bouncycastle:bcpkix-jdk18on:$bouncyCastleVersion")

    // API Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(module = "mockito-core")
    }
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("io.mockk:mockk:$mockKVersion")
    testImplementation(kotlin("test"))

    // Testcontainers
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("com.zaxxer:HikariCP:5.1.0")

    // REST API testing
    testImplementation("io.rest-assured:rest-assured:5.3.0")
    testImplementation("io.rest-assured:kotlin-extensions:5.3.0")
    testImplementation("io.rest-assured:json-path:5.3.0")
    testImplementation("io.rest-assured:xml-path:5.3.0")

    // Test data generation
    testImplementation("io.github.serpro69:kotlin-faker:1.16.0")

    // Integration test specific dependencies
    "integrationTestImplementation"(sourceSets.test.get().output)
}

ktlint {
    version.set("1.5.0")
    android.set(false)
    ignoreFailures.set(false)
    enableExperimentalRules.set(true)
    filter {
        exclude { element ->
            element.file.path.contains("generated/") ||
                element.file.path.contains("build/")
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/config/detekt/detekt.yaml")
    source.setFrom(
        "src/main/kotlin",
        "src/test/kotlin",
    )
}

configurations.matching { it.name == "detekt" }.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            useVersion(io.gitlab.arturbosch.detekt.getSupportedKotlinVersion())
        }
    }
}

dependencyAnalysis {
    issues {
        all {
            onAny {
                severity("fail")
            }
        }
    }
}

tasks.withType<com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version) && !isNonStable(currentVersion)
    }
}

fun isNonStable(version: String): Boolean {
    val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.uppercase().contains(it) }
    val regex = "^[0-9,.v-]+(-r)?$".toRegex()
    val isStable = stableKeyword || regex.matches(version)
    return isStable.not()
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter("test")
}

tasks.check { dependsOn(integrationTest) }
