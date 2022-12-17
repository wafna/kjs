// KJS

group = "wafna"
version = "0.0.1-SNAPSHOT"

// The production browser is bundled into the server distribution.
listOf(":server:distZip", ":server:distTar").forEach { dep ->
    tasks.getByPath(dep).dependsOn(":browser:build")
}