import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
    dependencies {
        classpath("com.netflix.nebula:nebula-release-plugin:16.0.0")
    }
}

plugins {
    kotlin("jvm") version "1.6.10"
    id("com.jfrog.artifactory") version "4.26.2"
    `maven-publish`
    jacoco
    id("org.jetbrains.dokka") version "1.6.10" apply false
}

// Conditionally enable dokka only when dokkaEnabled=true property is set.
// Latest version 1.6.10 depends on vulnerable versions of jackson/jsoup/etc.
val dokkaEnabled = (project.properties["dokkaEnabled"]?.toString()?.toBoolean()) ?: false
project.logger.lifecycle("dokkaEnabled = $dokkaEnabled")

if (dokkaEnabled) {
    apply(plugin = "org.jetbrains.dokka")
}

apply(plugin = "nebula.release")

group = "com.target"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.github.microutils:kotlin-logging:2.1.21")
    implementation("org.objenesis:objenesis:3.2")
    implementation("io.micrometer:micrometer-core:1.8.1")
    api("com.github.ben-manes.caffeine:caffeine:3.0.5")

    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.17")
    testImplementation("org.spekframework.spek2:spek-runner-junit5:2.0.17")
    testImplementation("io.mockk:mockk:1.12.2")
    testImplementation(platform("org.junit:junit-bom:5.8.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("ch.qos.logback:logback-classic:1.2.10")
    testImplementation("com.google.guava:guava-testlib:31.0.1-jre")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

java {
    withSourcesJar()
}

jacoco {
    toolVersion = "0.8.7"
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
        csv.isEnabled = false
        html.destination = file("${buildDir}/jacocoHtml")
    }
}