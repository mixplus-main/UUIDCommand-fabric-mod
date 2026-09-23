plugins {
    id("fabric-loom") version "1.17-SNAPSHOT"
    id("maven-publish")
}

version = project.property("mod_version")!!
group = project.property("maven_group")!!

base {
    archivesName.set(project.property("archives_base_name") as String)
}

loom {
    splitEnvironmentSourceSets()

    mods {
        create("uuidcommand") {
            sourceSet("main")
            sourceSet("client")
        }
    }
}

fabricApi {
    configureDataGeneration {
        client = true
    }
}

repositories {
    maven {
        name = "GithubPackages"
        url = uri("https://maven.pkg.github.com/mixplus-main/mixplus-library")

        credentials {
            username = System.getenv("GITHUB_USERNAME")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    implementation("com.mixplus.library:library:1.14.33-SNAPSHOT")?.let { include(it) }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("loader_version", project.property("loader_version"))

    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "version" to project.version!!,
                "minecraft_version" to project.property("minecraft_version")!!,
                "loader_version" to project.property("loader_version")!!
            )
        )
    }
}

val targetJavaVersion = 21

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"

    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)

    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(
            JavaLanguageVersion.of(targetJavaVersion)
        )
    }

    withSourcesJar()
}

tasks.jar {
    from("LICENSE") {
        rename {
            "${it}_${project.property("archives_base_name")}"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name") as String
            from(components["java"])
        }
    }

    repositories {
    }
}