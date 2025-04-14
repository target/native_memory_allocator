import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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

//    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.17")
//    testImplementation("org.spekframework.spek2:spek-runner-junit5:2.0.17")
//    testImplementation("io.mockk:mockk:1.12.2")
//    testImplementation(platform("org.junit:junit-bom:5.8.2"))
//    testImplementation("org.junit.jupiter:junit-jupiter")
//    testImplementation("ch.qos.logback:logback-classic:1.2.10")
//    testImplementation("com.google.guava:guava-testlib:31.0.1-jre")
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