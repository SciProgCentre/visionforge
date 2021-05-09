plugins {
    id("ru.mipt.npm.gradle.js")
}

val dataforgeVersion: String by rootProject.extra

kotlin{
    js{
        useCommonJs()
    }
}

dependencies{
    api(project(":ui:react"))

    implementation(npm("@jetbrains/icons", "3.14.1"))
    implementation(npm("@jetbrains/ring-ui", "4.0.7"))
}