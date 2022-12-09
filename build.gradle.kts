// Master build for JobLeader.

group = "wafna"
version = "0.0.1-SNAPSHOT"

tasks.getByPath(":server:distZip").dependsOn(":browser:build")
tasks.getByPath(":server:distTar").dependsOn(":browser:build")