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
        archiveBaseName.set("referencecount-shadowjar")
        manifest {
            attributes(mapOf("Main-Class" to "com.target.nativememoryallocator.examples.map.referencecount.ReferenceCountKt"))
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}