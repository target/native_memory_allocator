buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-library`
    `maven-publish`
    signing
    alias(libs.plugins.nexusPublishPlugin)
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
        toolchain { languageVersion.set(JavaLanguageVersion.of(jvmTargetVersion)) }
    }

    withType<Test> {
        useJUnitPlatform()
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    repositories {
        maven {
            credentials(PasswordCredentials::class)
            name = "sonatype" // correlates with the environment variable set in the github action release.yml publish job

            val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
            setUrl(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
        }
    }

    publications {
        val projectTitle: String by project
        val projectDescription: String by project
        val projectUrl: String by project
        val projectScm: String by project

        create<MavenPublication>("mavenJava") {
            artifactId = rootProject.name
            from(components["java"])
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            pom {
                name.set(projectTitle)
                description.set(projectDescription)
                url.set(projectUrl)
                licenses {
                    license {
                        name.set("Apache 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("ossteam")
                        name.set("OSS Office")
                        email.set("ossteam@target.com")
                    }
                }
                scm {
                    url.set(projectScm)
                }
            }
        }
    }

}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    if (signingKey.isNullOrBlank() || signingPassword.isNullOrBlank()) {
        isRequired = false
    } else {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications)
    }
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}