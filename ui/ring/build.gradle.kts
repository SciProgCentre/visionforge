plugins {
    id("ru.mipt.npm.js")
}

val dataforgeVersion: String by rootProject.extra

kotlin{
    js{
        useCommonJs()
    }
}

dependencies{
    api(project(":ui:react"))

    implementation(npm("@jetbrains/logos", "1.1.6"))
    implementation(npm("@jetbrains/ring-ui", "3.0.13"))
    implementation(npm("svg-inline-loader", "0.8.0"))
}