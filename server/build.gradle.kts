import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    id("org.jetbrains.dokka") version "1.6.10"
    application
}

val ktorVersion = "2.1.3"

dependencies {
    // Web
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-gson:$ktorVersion")
    // DB
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("com.h2database:h2:2.1.214")
    implementation("org.flywaydb:flyway-core:9.8.3")

    implementation(project(":util"))
    implementation(project(":domain"))

    testImplementation(kotlin("test"))
}

repositories {
    mavenCentral()
}

tasks.test {
    useTestNG()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("wafna.kjs.server.ServerKt")
}

distributions {
    main {
        distributionBaseName.set("kjs")
        contents {
            from(rootProject.file("browser/build/distributions")) {
                into("browser")
            }
        }
    }
}