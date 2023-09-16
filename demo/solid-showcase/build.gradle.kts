plugins {
    id("space.kscience.gradle.mpp")
//    application
}

kscience {
    useCoroutines()
    jvm()
    js{
        binaries.executable()
    }
    dependencies {
        implementation(projects.visionforgeSolid)
        implementation(projects.visionforgeGdml)
    }

    jsMain {
        implementation(projects.visionforgeThreejs)
    }
}

kotlin.explicitApi = null

//application {
//    mainClass.set("space.kscience.visionforge.solid.demo.FXDemoAppKt")
//}