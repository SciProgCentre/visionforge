plugins {
    id("ru.mipt.npm.mpp")
}

val plotlyVersion = "0.3.1"

kscience{
    useSerialization()
}

kotlin {
    js{
        //binaries.library()
        binaries.executable()
        browser {
            webpackTask {
                this.outputFileName = "js/visionforge-three.js"
            }
        }
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
                api(project(":visionforge-core"))
                api("kscience.plotlykt:plotlykt-core:${plotlyVersion}")
            }
        }
    }
}