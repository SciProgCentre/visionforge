plugins {
    id("space.kscience.gradle.mpp")
    `maven-publish`
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

application {
    mainClass.set("space.kscience.visionforge.solid.demo.FXDemoAppKt")
}

kotlin{
    explicitApi = null
}