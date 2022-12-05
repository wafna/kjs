import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    id("org.jetbrains.dokka") version "1.6.10"
    application
}

val ktorVersion = "2.1.3"

dependencies {
    implementation(project(":util"))
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
    mainClass.set("wafna.kwap.server.ServerKt")
}

distributions {
    main {
        distributionBaseName.set("kwap")
        contents {
            from(file("configs")) {
                into("configs")
            }
            from(rootProject.file("browser/build")) {
                into("browser")
            }
        }
    }
}