import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
}

group = "com.target"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenLocal()
    maven {
        url = uri("https://binrepo.target.com/artifactory/TargetOSS")
        metadataSources {
            artifact()
        }
    }
    maven { setUrl("https://binrepo.target.com/artifactory/maven-central") }
    maven { setUrl("https://binrepo.target.com/artifactory/platform") }
    maven { setUrl("https://binrepo.target.com/artifactory/jcenter") }
    maven { setUrl("https://binrepo.target.com/artifactory/gradle") }
    maven { setUrl("https://binrepo.target.com/artifactory/libs-release") }
    maven { setUrl("https://binrepo.target.com/artifactory/availability") }
    maven { setUrl("https://binrepo.target.com/artifactory/esv-deploy") }
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.12.5")
    implementation("io.github.microutils:kotlin-logging:2.0.11")
    implementation("org.objenesis:objenesis:3.2")

    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.17")
    testImplementation("org.spekframework.spek2:spek-runner-junit5:2.0.17")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation(platform("org.junit:junit-bom:5.8.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
