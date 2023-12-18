plugins {
    id("space.kscience.gradle.mpp")
}

kscience {
    useCoroutines()
}

kotlin {
    explicitApi = null
    js {
        browser {
            binaries.executable()
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