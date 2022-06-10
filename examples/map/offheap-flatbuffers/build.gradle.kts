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
    implementation("com.google.flatbuffers:flatbuffers-java:2.0.3")
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("offheap-flatbuffers-shadowjar")
        manifest {
            attributes(mapOf("Main-Class" to "com.target.nativememoryallocator.examples.map.offheap.flatbuffers.OffHeapFlatBuffersKt"))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}