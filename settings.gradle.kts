pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://maven.minecraftforge.net/") {
            name = "MinecraftForge"
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

include("fabric", "forge")