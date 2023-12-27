plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose)
}

val ktorVersion: String by rootProject.extra

kscience {
    fullStack("js/visionforge-three.js")

    commonMain {
        api(projects.visionforgeSolid)
        api(projects.visionforgeComposeHtml)
    }

    jvmMain{
        api(projects.visionforgeServer)
    }

    jsMain{
        api(projects.visionforgeThreejs)
        implementation(npm("file-saver","2.0.5"))
        implementation(npm("@types/file-saver", "2.0.7"))
        compileOnly(npm("webpack-bundle-analyzer","4.5.0"))
    }
}