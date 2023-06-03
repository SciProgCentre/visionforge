plugins {
    id("space.kscience.gradle.js")
}

kscience{
    useCoroutines()
}

kotlin{
    explicitApi = null
    js(IR){
        useCommonJs()
        browser {
            binaries.executable()
            commonWebpackConfig {
                cssSupport{
                    enabled.set(false)
                }
            }
        }
    }
}


dependencies{
    implementation(projects.visionforge.visionforgeGdml)
    implementation(projects.visionforge.visionforgePlotly)
    implementation(projects.visionforge.visionforgeMarkdown)
    implementation(projects.visionforge.visionforgeThreejs)
    implementation(projects.ui.ring)
}