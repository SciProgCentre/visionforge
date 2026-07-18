plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose.compiler)
    alias(spclibs.plugins.compose.jb)
}

kscience {
    jvm()
    useCoroutines()

    commonMain{
        implementation(projects.visionforgeSolid)
    }

    jvmMain {
        implementation(projects.visionforgeComposeMultiplatform)
    }
}

kotlin{
    explicitApi = null
    sourceSets{
        commonMain{
            dependencies {
                implementation(compose.desktop.currentOs)
                api(libs.compose.preview)
            }
        }
    }
}


compose{
    desktop{
        desktop {
            application {
                mainClass = "MainKt"
            }
        }
    }
}