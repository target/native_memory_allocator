import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":examples:map:utils"))
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("offheap-eviction-shadowjar")
        manifest {
            attributes(mapOf("Main-Class" to "com.target.nativememoryallocator.examples.map.offheap.eviction.OffHeapEvictionKt"))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}