plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose.compiler)
    alias(spclibs.plugins.compose.jb)
    `maven-publish`
}

kscience {
    fullStack(
        bundleName = "js/plotly-kt-server.js",
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
        api(projects.visionforgeComposeHtml)
        api(projects.plotlyKt.plotlyKtCore)
    }

    jvmMain {
        api(projects.visionforgeServer)
        api(project.dependencies.platform(spclibs.ktor.bom))
        api("io.ktor:ktor-server-cio")
    }
}