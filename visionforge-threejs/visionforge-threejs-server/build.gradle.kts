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
        }
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":visionforge-solid"))
            }
        }
        jvmMain {
            dependencies {
                api(project(":visionforge-server"))
            }
        }
        jsMain {
            dependencies {
                api(project(":visionforge-threejs"))
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
