plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jmh)

}

repositories {
    mavenCentral()
}

dependencies {
    implementation(rootProject)
    implementation(libs.kotlin.logging)
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