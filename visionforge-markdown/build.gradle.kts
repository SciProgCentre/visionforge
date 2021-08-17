plugins {
    id("ru.mipt.npm.gradle.mpp")
}

val markdownVersion = "0.2.4"

kscience {
    useSerialization()
}

kotlin {
    js {
        //binaries.library()
        binaries.executable()
        browser {
            webpackTask {
                outputFileName = "js/visionforge-markdown.js"
            }
        }
    }

    jvm {
        val processResourcesTaskName =
            compilations[org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.MAIN_COMPILATION_NAME]
                .processResourcesTaskName
    }


    val jsBrowserDistribution by tasks.getting

    tasks.getByName<ProcessResources>("jvmProcessResources") {
        dependsOn(jsBrowserDistribution)
        from(jsBrowserDistribution)
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":visionforge-core"))
                api("org.jetbrains:markdown:$markdownVersion")
            }
        }
    }
}