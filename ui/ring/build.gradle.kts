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
    api(project(":ui:react"))

    implementation(npm("@jetbrains/logos", "1.1.6"))
    implementation(npm("@jetbrains/ring-ui", "3.0.13"))


    implementation(npm("svg-inline-loader", "0.8.0"))
}