# Setting up the True Adaptive Music API

!!! warning

    Bridge mods should only be written in Kotlin.

Implementing either modded predicates or events (or both) requires creating a **Bridge Mod**. That is, a mod that "bridges" the functionalities of two mods. If you want to create bridge mod for a mod that is using forge/neoforge, this can still work and the setup is mostly the same. If you have any questions about it, I would recommend asking in the discord. If you are the creator of the mod you want to make the bridge for, you could also just follow the steps below as an integration (similar to adding ModMenu support) directly into your mod.

First, make a new mod (or open up your existing mod project if you are the mod owner). I personally recommend using the Minecraft Development plugin with Intellij as it makes the boilerplate steps very easy. However you do it, you'll want to make a mod for the Minecraft version you are using and for fabric.

Once your environment is loaded, you'll want to get either the gradle reference or jar file for your mod and the True Adaptive Music API mod gradle reference (or jar) which can be found on curseforge (reference and jar) or modrinth (jar only).

To get the gradle reference for the True Adaptive Music API:
1. Set up [cursemaven](https://www.cursemaven.com) for your project.
2. Select the latest file for your minecraft version range [here](https://www.curseforge.com/minecraft/mc-mods/trueadaptivemusicapi/files/all) and copy the "Curse Maven Snippet" into your `build.gradle`.

Then do the same as above for the mod jar. If the mod doesn't have a maven source, you can still add it to your `build.gradle` with `modImplementation(files(JAR_FILE_NAME))` while the file is in your mod project directory.

It is recommended to use `modApi` instead of `modImplementation` for the API mod (or `api` if you are using official Mojang mappings in Minecraft 26.1+).

Finally make sure to download sources for gradle and you're good to go! In IntelliJ this can be done by opening the gradle window and clicking the download icon:
![Gradle sources download](gradle.png)

After that task finishes, you're ready to create your custom [predicates](Creating%20a%20Predicate%20Type.md) or [events](Creating%20an%20Event%20Type.md)!

If you are creating an event, I would still recommend reading the predicate tutorial first, as events are similar but general more complicated to implement.