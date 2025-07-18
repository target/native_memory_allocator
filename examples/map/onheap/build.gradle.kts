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
        archiveBaseName.set("onheap-shadowjar")
        manifest {
            attributes(mapOf("Main-Class" to "com.target.nativememoryallocator.examples.map.onheap.OnHeapKt"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}