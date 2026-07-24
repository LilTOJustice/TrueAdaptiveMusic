import org.gradle.kotlin.dsl.kotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.3.20"
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT"
    id("maven-publish")
}

version = "${project.property("mod_version") as String}+${project.property("minecraft_version")}"
group = project.property("mod_group_id") as String

val isCurseForge = project.property("is_curseforge") == "true"
base {
    archivesName.set(
        project.property("archives_base_name") as String + "-fabric" + if (isCurseForge) "-cf" else "")
}

val targetJavaVersion = (project.property("java_version") as String).toInt()
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("trueadaptivemusic") {
            sourceSet("main")
            sourceSet("client")
        }
    }

    accessWidenerPath = file("src/main/resources/trueadaptivemusic.accesswidener")
}

repositories {
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://cursemaven.com") {
        name = "CurseMaven"
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${project.property("fabric_loader_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("fabric_kotlin_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_api_version")}")

    modApi("com.terraformersmc:modmenu:${project.property("modMenu_version")}")
    modApi("curse.maven:trueadaptivemusicapi-1514598:8442457")
    include("curse.maven:trueadaptivemusicapi-1514598:8442457")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("fabric_loader_version", project.property("fabric_loader_version"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "mod_id" to project.property("mod_id")!!,
            "mod_version" to project.version,
            "mod_name" to project.property("mod_name")!!,
            "mod_description" to project.property("mod_description")!!,
            "mod_authors" to project.property("mod_authors")!!,
            "mod_home" to project.property("mod_home")!!,
            "mod_issues" to project.property("mod_issues")!!,
            "mod_source" to project.property("mod_source")!!,
            "mod_license" to project.property("mod_license")!!,
            "mod_icon" to project.property("mod_icon")!!,
            "minecraft_version" to project.property("minecraft_version")!!,
            "minecraft_version_range" to project.property("minecraft_version_range")!!,
            "loader_version" to project.property("fabric_loader_version")!!,
            "kotlin_loader_version" to project.property("fabric_kotlin_version")!!
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName}" }
    }
}

sourceSets {
    named("main") {
        java {
            srcDirs("../common/src/main/java")
        }
        kotlin {
            srcDirs("../common/src/main/kotlin")
        }
        resources {
            srcDirs("../common/src/main/resources")
        }
    }
    named("client") {
        java {
            srcDirs("../common/src/client/java")

            if (!isCurseForge) {
                srcDirs("../extensions/src/main/java")
            }
        }
        kotlin {
            srcDirs("../common/src/client/kotlin")

            if (!isCurseForge) {
                srcDirs("../extensions/src/main/kotlin")
            }
        }
        resources {
            srcDirs("../common/src/client/resources")

            if (!isCurseForge) {
                srcDirs("../extensions/src/main/resources")
            }
        }
    }
}
