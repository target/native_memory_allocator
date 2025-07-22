buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm)
//    `maven-publish`
}

val jvmTargetVersion: String by project

group = "com.target"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.logging)
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