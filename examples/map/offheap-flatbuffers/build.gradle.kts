plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(project(":examples:map:utils"))
    implementation(libs.flatbuffers.java)
}

tasks {
    shadowJar {
        archiveBaseName.set("offheap-flatbuffers-shadowjar")
        manifest {
            attributes(mapOf("Main-Class" to "com.target.nativememoryallocator.examples.map.offheap.flatbuffers.OffHeapFlatBuffersKt"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}