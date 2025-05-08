plugins {
    kotlin("jvm") version libs.versions.kotlin.core
    alias(libs.plugins.jmh)

}

repositories {
    mavenCentral()
}

dependencies {
    implementation(rootProject)
    implementation(libs.kotlinlogging)
    implementation(libs.logback.classic)
    implementation(libs.rocksdb.jni)
    implementation(libs.jmh.core)
}

jmh {
    warmupIterations.set(5)
    iterations.set(5)
    fork.set(1)
    forceGC.set(true)
    failOnError.set(true)
    jvmArgs.set(listOf("-Xmx4G"))
}