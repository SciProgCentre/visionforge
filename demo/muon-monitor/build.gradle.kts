plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose.compiler)
    alias(spclibs.plugins.compose.jb)
}

group = "ru.mipt.npm"


kscience {
    fullStack(
        "muon-monitor.js",
        development = false,
        jvmConfig = {
            application("ru.mipt.npm.muon.monitor.MMServerKt")
        },
        browserConfig = {
            commonWebpackConfig {
                cssSupport {
                    enabled = true
                }
                scssSupport {
                    enabled = true
                }
            }
        }
    )

    useCoroutines()
    useSerialization()

    commonMain {
        implementation(projects.visionforgeSolid)
        implementation(projects.visionforgeComposeHtml)
    }
    jvmMain {
        implementation("org.apache.commons:commons-math3:3.6.1")
        implementation("io.ktor:ktor-server-cio")
        implementation("io.ktor:ktor-server-content-negotiation")
        implementation("io.ktor:ktor-serialization-kotlinx-json")
        implementation(spclibs.logback.classic)
    }
    jsMain {
        implementation(projects.visionforgeThreejs)
    }
}

kotlin {
    explicitApi = null
}