allprojects {
    version = rootProject.property("mod_version")!!
    group = rootProject.property("maven_group")!!


    repositories {
        mavenCentral()
        maven {
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/mixplus-main/mixplus-library")

            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

subprojects {

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}