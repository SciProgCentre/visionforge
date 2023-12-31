plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose)
}

val ktorVersion: String by rootProject.extra

kscience {
    fullStack(
        bundleName = "js/visionforge-three.js",
        browserConfig = {
            webpackTask {
                cssSupport {
                    enabled = true
                }
                scssSupport {
                    enabled = true
                }
            }
        }
    )

    commonMain {
        api(projects.visionforgeSolid)
        api(projects.visionforgeComposeHtml)
    }

    jvmMain {
        api(projects.visionforgeServer)
    }

    jsMain {
        api(projects.visionforgeThreejs)
        implementation(npm("file-saver", "2.0.5"))
        implementation(npm("@types/file-saver", "2.0.7"))
        compileOnly(npm("webpack-bundle-analyzer", "4.5.0"))
    }
}