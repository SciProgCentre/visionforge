plugins {
    id("space.kscience.gradle.mpp")
}

val ktorVersion: String by rootProject.extra

kotlin {
    js(IR) {
        browser {
            webpackTask {
                this.outputFileName = "js/visionforge-three.js"
            }
            commonWebpackConfig {
                cssSupport{
                    enabled.set(false)
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(projects.visionforgeSolid)
            }
        }
        jvmMain {
            dependencies {
                api(projects.visionforgeServer)
            }
        }
        jsMain {
            dependencies {
                api(projects.visionforgeThreejs)
                api(projects.ui.ring)
            }
        }
    }
}


val jsBrowserDistribution by tasks.getting
val jsBrowserDevelopmentExecutableDistribution by tasks.getting

val devMode = rootProject.findProperty("visionforge.development") as? Boolean
    ?: rootProject.version.toString().contains("dev")

tasks.getByName<ProcessResources>("jvmProcessResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    if (devMode) {
        dependsOn(jsBrowserDevelopmentExecutableDistribution)
        from(jsBrowserDevelopmentExecutableDistribution)
    } else {
        dependsOn(jsBrowserDistribution)
        from(jsBrowserDistribution)
    }
}
