plugins {
    kotlin("jvm")
    id("me.champeau.jmh") version "0.6.8"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(rootProject)
    implementation("io.github.microutils:kotlin-logging:2.1.21")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("org.rocksdb:rocksdbjni:7.9.2")
    implementation("org.openjdk.jmh:jmh-core:1.35")
}

jmh {
    warmupIterations.set(3)
    iterations.set(3)
    fork.set(1)
    forceGC.set(true)
    failOnError.set(true)
    jvmArgs.set(listOf("-Xmx2G"))
}