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
}

tasks {
    shadowJar {
        archiveBaseName.set("offheap-shadowjar")
        manifest {
            attributes(mapOf("Main-Class" to "com.target.nativememoryallocator.examples.map.offheap.OffHeapKt"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}