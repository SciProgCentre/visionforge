plugins {
    id("ru.mipt.npm.js")
}

val dataforgeVersion: String by rootProject.extra

dependencies{
    api(project(":ui:react"))
}