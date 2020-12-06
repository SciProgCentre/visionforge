plugins {
    id("ru.mipt.npm.mpp")
    application
}


group = "ru.mipt.npm"

//val kvisionVersion: String = "3.16.2"

kscience{
    useSerialization{
        json()
    }
    application()
}

val ktorVersion: String by rootProject.extra

kotlin {
    afterEvaluate {
        val jsBrowserDistribution by tasks.getting

        jvm {
            withJava()
            compilations[org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME]?.apply {
                tasks.getByName<ProcessResources>(processResourcesTaskName) {
                    dependsOn(jsBrowserDistribution)
                    afterEvaluate {
                        from(jsBrowserDistribution)
                    }
                }
            }

        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":visionforge-solid"))
            }
        }
        jvmMain {
            dependencies {
                implementation(project(":visionforge-server"))
            }
        }
        jsMain {
            dependencies {
                implementation(project(":visionforge-threejs"))
            }
        }
    }
}

application {
    mainClass.set("ru.mipt.npm.sat.SatServerKt")
}
