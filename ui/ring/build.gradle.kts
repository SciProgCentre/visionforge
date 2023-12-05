plugins {
    id("space.kscience.gradle.mpp")
}

val dataforgeVersion: String by rootProject.extra

kscience{
    js{
        useCommonJs()
        browser {
            commonWebpackConfig {
                cssSupport{
                    enabled.set(false)
                }
            }
        }
    }
    jsMain{
        api(projects.ui.react)
        api("org.jetbrains.kotlin-wrappers:kotlin-ring-ui")

        implementation(npm("core-js","3.12.1"))
        implementation(npm("file-saver", "2.0.2"))
    }
}