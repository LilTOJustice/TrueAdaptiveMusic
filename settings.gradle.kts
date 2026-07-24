pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://maven.neoforged.net/releases") {
            name = "NeoForge"
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

include("fabric", "neoforge")