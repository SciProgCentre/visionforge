plugins {
    id("ru.mipt.npm.mpp")
 }

val ktorVersion: String by rootProject.extra

kotlin {
    js{
        browser {
            webpackTask {
                this.outputFileName = "js/visionforge-three.js"
            }
        }
        binaries.executable()
    }

    afterEvaluate {
        val jsBrowserDistribution by tasks.getting

        tasks.getByName<ProcessResources>("jvmProcessResources") {
            dependsOn(jsBrowserDistribution)
            afterEvaluate {
                from(jsBrowserDistribution)
            }
        }
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