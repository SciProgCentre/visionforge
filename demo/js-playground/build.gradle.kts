plugins {
    id("ru.mipt.npm.gradle.js")
}

kscience{
    useCoroutines()
    application()
}

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
    implementation(projects.visionforge.visionforgeGdml)
    implementation(projects.visionforge.visionforgePlotly)
    implementation(projects.visionforge.visionforgeMarkdown)
    implementation(projects.visionforge.visionforgeThreejs)
    implementation(projects.ui.ring)
}