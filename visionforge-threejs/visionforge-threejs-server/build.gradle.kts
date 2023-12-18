plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose)
}

val ktorVersion: String by rootProject.extra

kscience {
    fullStack("js/visionforge-three.js")

    commonMain {
        api(projects.visionforgeSolid)
        api(projects.visionforgeCompose)
    }

    jvmMain{
        api(projects.visionforgeServer)
    }

    jsMain{
        api(projects.visionforgeThreejs)
        compileOnly(npm("webpack-bundle-analyzer","4.5.0"))
    }
}