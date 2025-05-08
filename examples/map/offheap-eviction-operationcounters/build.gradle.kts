plugins {
    kotlin("jvm") version libs.versions.kotlin.core
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
            attributes(mapOf("Main-Class" to "com.target.nativememoryallocator.examples.map.offheap.eviction.operationcounters.OffHeapEvictionOperationCountersKt"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}