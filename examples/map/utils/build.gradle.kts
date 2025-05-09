plugins {
    kotlin("jvm") version libs.versions.kotlin.core
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    api(rootProject)
    api(libs.kotlin.coroutines)
    api(libs.kotlin.logging)
    api(libs.logback.classic)
}