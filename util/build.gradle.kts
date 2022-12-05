import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    id("org.jetbrains.dokka") version "1.6.10"
    id("java-library")
}

repositories {
    mavenCentral()
}

// Version should match kotlin version in plugins, above.
val kotlinVersion = "1.7.20"

dependencies {
    api("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    api("com.typesafe:config:1.4.2")
    api("org.slf4j:slf4j-api:2.0.5")
    api("ch.qos.logback:logback-classic:1.4.5")
    api("io.arrow-kt:arrow-core:1.1.2")
    api("commons-codec:commons-codec:1.15")

    testImplementation(kotlin("test"))
}

tasks.test {
    useTestNG()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}
