pluginManagement {
    repositories {
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        gradlePluginPortal()
    }
}

include("common")
include(":fabric-1.21.8")
include(":fabric-1.21.11")