buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

plugins {
    kotlin("jvm") version libs.versions.kotlin.core
//    `maven-publish`
}

val jvmTargetVersion: String by project

group = "com.target"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinlogging)
    implementation(libs.objnesis)
    implementation(libs.micrometer.core)
    api(libs.caffeine)

    testImplementation(rootProject.libs.bundles.testing)
}

tasks {
    java {
        withSourcesJar()
        toolchain { languageVersion.set(JavaLanguageVersion.of(jvmTargetVersion)) }
    }

    withType<Test> {
        useJUnitPlatform()
    }
}

//publishing {
//    repositories {
//        maven {
//            name = "GitHubPackages"
//            url = uri("https://maven.pkg.github.com/target/native_memory_allocator")
//            credentials {
//                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
//                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
//            }
//        }
//    }
//    publications {
//        register<MavenPublication>("gpr") {
//            from(components["java"])
//        }
//    }
//}