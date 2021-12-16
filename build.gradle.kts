import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val compileKotlin: KotlinCompile by tasks
val compileTestKotlin: KotlinCompile by tasks

val junitJupiterVersion: String by project

group = "org.svaor.tutorial"
version = "1.0-SNAPSHOT"
description = "YAML Spec tutorial"

plugins {
    kotlin("jvm") version "1.5.32"
}

compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:1.7.32")

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.13.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.13.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.0")

    implementation(kotlin("stdlib-jdk8"))

    testImplementation(kotlin("reflect"))
    testImplementation(kotlin("stdlib-jdk8"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("ch.qos.logback:logback-classic:1.2.7")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
}

tasks.test {
    useJUnitPlatform()
}
