subprojects {
    layout.buildDirectory.set(rootProject.layout.buildDirectory.dir(name))

    repositories {
        maven("https://cursemaven.com") {
            name = "CurseMaven"
        }
    }
}