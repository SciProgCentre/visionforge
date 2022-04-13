plugins {
    id("ru.mipt.npm.gradle.js")
}

val dataforgeVersion: String by rootProject.extra

kotlin{
    js(IR){
        useCommonJs()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = false
            }
        }
    }
}

dependencies{
    api(project(":ui:react"))
    api("org.jetbrains.kotlin-wrappers:kotlin-ring-ui")

    implementation(npm("core-js","3.12.1"))
    implementation(npm("file-saver", "2.0.2"))
}