plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose)
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
                api(compose.preview)
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