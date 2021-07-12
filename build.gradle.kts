import com.github.vlsi.gradle.properties.dsl.props
import com.github.vlsi.gradle.properties.dsl.stringProperty
import com.github.vlsi.gradle.publishing.dsl.simplifyXml
import com.github.vlsi.gradle.publishing.dsl.versionFromResolution
import org.gradle.api.publish.maven.internal.publication.MavenPublicationInternal

plugins {
    id("com.github.vlsi.gradle-extensions")
    id("com.github.vlsi.stage-vote-release")
    id("maven-publish")
}

val String.v: String get() = rootProject.extra["$this.version"] as String
val projectVersion = project.name.v

group = "com.github.weisj"
version = projectVersion

releaseParams {
    tlp.set("JavaNativeFoundation")
    gitRepoName.set(tlp.get())
    organizationName.set("weisj")
    componentName.set("JavaNativeFoundation")
    prefixForProperties.set("gh")
    svnDistEnabled.set(false)
    sitePreviewEnabled.set(false)
    nexus {
        mavenCentral()
    }
    voteText.set {
        """
        ${it.componentName} v${it.version}-rc${it.rc} is ready for preview.
        Git SHA: ${it.gitSha}
        Staging repository: ${it.nexusRepositoryUri}
        """.trimIndent()
    }
}

val buildJNF by tasks.registering(Exec::class) {
    commandLine("sh", "build_jnf.sh")
}

val archiveJNF by tasks.registering(Zip::class) {
    dependsOn(buildJNF)
    archiveFileName.set("JavaNativeFoundation.framework.zip")
    destinationDirectory.set(project.buildDir.resolve("frameworks"))
    from("buildNative/Frameworks/JavaNativeFoundation.framework")
}

fun registerJNFConfiguration(architecture : String) = configurations.registering {
    isCanBeResolved = false
    isCanBeConsumed = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "${Usage.C_PLUS_PLUS_API}+${Usage.NATIVE_LINK}+${Usage.NATIVE_RUNTIME}"))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements::class.java, "framework-bundle"))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category::class.java, Category.LIBRARY))
        attribute(Attribute.of("dev.nokee.operatingSystem", String::class.java), "macos")
        attribute(Attribute.of("dev.nokee.architecture", String::class.java), architecture)
    }
    outgoing.artifact(archiveJNF)
}

val jnfElementsArm by registerJNFConfiguration("arm64")
val jnfElementsX86 by registerJNFConfiguration("x86-64")

apply(from="jnf-component.gradle")

publishing {
    val useInMemoryKey by props()
    if (useInMemoryKey) {
        apply(plugin = "signing")

        configure<SigningExtension> {
            useInMemoryPgpKeys(
                project.stringProperty("signing.inMemoryKey")?.replace("#", "\n"),
                project.stringProperty("signing.password")
            )
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["jnf"])
            (this as MavenPublicationInternal).publishWithOriginalFileName()
        }
        withType<MavenPublication> {
            // Use the resolved versions in pom.xml
            // Gradle might have different resolution rules, so we set the versions
            // that were used in Gradle build/test.
            versionFromResolution()
            pom {
                simplifyXml()

                description.set(
                    project.description
                        ?: "The JavaNativeFoundation framework"
                )
                name.set(
                    (project.findProperty("artifact.name") as? String)
                        ?: project.name.capitalize().replace("-", " ")
                )
                url.set("https://github.com/weisJ/JavaNativeFoundation")
                organization {
                    name.set("com.github.weisj")
                    url.set("https://github.com/weisj")
                }
                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/weisJ/JavaNativeFoundation/issues")
                }
                licenses {
                    license {
                        name.set("BSD-3")
                        url.set("https://github.com/weisJ/JavaNativeFoundation/blob/master/LICENSE")
                        distribution.set("repo")
                    }
                }
                scm {
                    url.set("https://github.com/weisJ/JavaNativeFoundation")
                    connection.set("scm:git:git://github.com/weisJ/JavaNativeFoundation.git")
                    developerConnection.set("scm:git:ssh://git@github.com:weisj/JavaNativeFoundation.git")
                }
                developers {
                    developer {
                        name.set("Jannis Weis")
                    }
                }
            }
        }
    }
}
