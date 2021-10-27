import groovy.lang.GroovyObject
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
        maven { url = uri("https://binrepo.target.com/artifactory/TargetOSS") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
    dependencies {
        classpath("com.netflix.nebula:nebula-release-plugin:16.0.0")
    }
}

plugins {
    kotlin("jvm") version "1.5.31"
    id("com.jfrog.artifactory") version "4.24.20"
    `maven-publish`
}

apply(plugin = "nebula.release")

group = "com.target.oss"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenLocal()
    maven {
        url = uri("https://binrepo.target.com/artifactory/TargetOSS")
        metadataSources {
            artifact()
        }
    }
    maven { setUrl("https://binrepo.target.com/artifactory/maven-central") }
    maven { setUrl("https://binrepo.target.com/artifactory/platform") }
    maven { setUrl("https://binrepo.target.com/artifactory/jcenter") }
    maven { setUrl("https://binrepo.target.com/artifactory/gradle") }
    maven { setUrl("https://binrepo.target.com/artifactory/libs-release") }
    maven { setUrl("https://binrepo.target.com/artifactory/esv-deploy") }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.12.5")
    implementation("io.github.microutils:kotlin-logging:2.0.11")
    implementation("org.objenesis:objenesis:3.2")
    api("com.github.ben-manes.caffeine:caffeine:3.0.4")

    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.17")
    testImplementation("org.spekframework.spek2:spek-runner-junit5:2.0.17")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation(platform("org.junit:junit-bom:5.8.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("ch.qos.logback:logback-classic:1.2.6")
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

publishing {
    repositories {
        mavenLocal()
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(tasks.findByName("sourcesJar"))
        }
    }
}

artifactory {
    setContextUrl("https://binrepo.target.com/artifactory")

    publish(delegateClosureOf<PublisherConfig> {
        repository(delegateClosureOf<GroovyObject> {
            val targetRepoKey = "TargetOSS"
            val username = project.findProperty("artifactoryPublishRepositoryUsername") ?: ""
            val password = project.findProperty("artifactoryPublishRepositoryPassword") ?: ""
            setProperty("repoKey", targetRepoKey)
            setProperty("username", username)
            setProperty("password", password)
            setProperty("maven", true)
        })
        defaults(delegateClosureOf<GroovyObject> {
            invokeMethod("publications", "mavenJava")
        })
    })

}