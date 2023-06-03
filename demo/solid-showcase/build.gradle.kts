plugins {
    id("space.kscience.gradle.mpp")
    application
}

kscience {
    useCoroutines()
    jvm {
        withJava()
    }
    js()
    dependencies {
        implementation(projects.visionforgeSolid)
        implementation(projects.visionforgeGdml)
    }

    jsMain {
        implementation(projects.visionforgeThreejs)
    }
}

kotlin.explicitApi = null

application {
    mainClass.set("space.kscience.visionforge.solid.demo.FXDemoAppKt")
}