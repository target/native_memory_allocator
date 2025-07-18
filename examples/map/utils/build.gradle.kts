plugins {
    alias(libs.plugins.kotlin.jvm)
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