plugins {
    id("scientifik.js")
}

val dataforgeVersion: String by rootProject.extra

kotlin {
    target {
        useCommonJs()
    }
}

dependencies{
    api(project(":dataforge-vis-common"))
    api(project(":ui:react"))
}