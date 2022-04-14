plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    api(rootProject)
    api("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.1")
    api("io.github.microutils:kotlin-logging:2.1.21")
    api("ch.qos.logback:logback-classic:1.2.11")
}