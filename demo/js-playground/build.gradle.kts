plugins {
    id("space.kscience.gradle.mpp")
}

kscience {
    useCoroutines()
}

kotlin {
    explicitApi = null
    js {
        useCommonJs()
        browser {
            binaries.executable()
            commonWebpackConfig {
                cssSupport {
                    enabled.set(false)
                }
            }
        }
    }
}

kscience {

    dependencies {
        implementation(projects.visionforge.visionforgeGdml)
        implementation(projects.visionforge.visionforgePlotly)
        implementation(projects.visionforge.visionforgeMarkdown)
        implementation(projects.visionforge.visionforgeThreejs)
    }
    jsMain {
        implementation(projects.ui.ring)
    }
}