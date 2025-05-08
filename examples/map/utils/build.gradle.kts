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
    api(libs.kotlinlogging)
    api(libs.logback.classic)
}