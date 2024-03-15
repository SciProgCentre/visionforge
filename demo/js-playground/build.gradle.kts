plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose)
}

kscience {
    useCoroutines()
}

kotlin {
    explicitApi = null
    js {
        browser {
            binaries.executable()
            commonWebpackConfig{
                cssSupport{
                    enabled = true
                }
                scssSupport{
                    enabled = true
                }
                sourceMaps = true
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
}