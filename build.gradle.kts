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

// Conditionally enable dokka only when dokkaEnabled=true property is set.
// Latest version 1.6.10 depends on vulnerable versions of jackson/jsoup/etc.
// TODO reenable dokka
//val dokkaEnabled = (project.properties["dokkaEnabled"]?.toString()?.toBoolean()) ?: false
//project.logger.lifecycle("dokkaEnabled = $dokkaEnabled")
//
//if (dokkaEnabled) {
//    apply(plugin = "org.jetbrains.dokka")
//}

val jvmTargetVersion: String by project

group = "com.target"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(libs.kotlinlogging)
    implementation(libs.objnesis)
    implementation(libs.micrometer.core)
    api(libs.caffeine)

    testImplementation(rootProject.libs.bundles.testing)
}

tasks {
    java { toolchain { languageVersion.set(JavaLanguageVersion.of(jvmTargetVersion)) } }

    withType<Test> {
        useJUnitPlatform()
    }
}

//java {
//    withSourcesJar()
//}

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