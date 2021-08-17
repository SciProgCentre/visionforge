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
    implementation(project(":visionforge-gdml"))
    implementation(project(":visionforge-plotly"))
    implementation(projects.visionforge.visionforgeMarkdown)
    implementation(project(":visionforge-threejs"))
    implementation(project(":ui:ring"))
}