plugins {
    id("com.github.vlsi.gradle-extensions")
    id("maven-publish")
}

val String.v: String get() = rootProject.extra["$this.version"] as String
val projectVersion = project.name.v

group = "com.github.weisj"
version = projectVersion

val buildJNF by tasks.registering(Exec::class) {
    commandLine("sh", "build_jnf.sh")
}

val archiveJNF by tasks.registering(Jar::class) {
    dependsOn(buildJNF)
    archiveBaseName.set(project.name)
    destinationDirectory.set(project.buildDir.resolve("frameworks"))
    from("buildNative/Frameworks/")
}

tasks.publish.configure { dependsOn(archiveJNF) }

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/weisj/JavaNativeFoundation")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            artifact(archiveJNF.get().outputs.files.singleFile) {
                builtBy(archiveJNF)
            }
        }
    }
}
