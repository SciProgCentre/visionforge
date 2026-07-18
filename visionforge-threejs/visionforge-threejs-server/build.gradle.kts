
plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose.compiler)
    alias(spclibs.plugins.compose.jb)
}

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
//        compileOnly(npm("webpack-bundle-analyzer", "4.5.0"))
    }
}