plugins {
    id("com.github.vlsi.gradle-extensions")
    id("maven-publish")
}

val String.v: String get() = rootProject.extra["$this.version"] as String
val projectVersion = "JavaNativeFoundation".v

group = "com.github.weisj"
version = projectVersion

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/OWNER/REPOSITORY")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        register("gpr") {
            //TODO
        }
    }
}
