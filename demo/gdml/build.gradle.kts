plugins {
    id("space.kscience.gradle.mpp")
}

group = "demo"

kscience {
    jvm()
    js {
        useCommonJs()
        browser {
            binaries.executable()
            commonWebpackConfig {
                cssSupport {
                    enabled.set(false)
                }
            }
        }
    }
    dependencies {
        implementation(projects.visionforgeSolid)
        implementation(projects.visionforgeGdml)
    }
    jvmMain {
//                implementation(project(":visionforge-fx"))
        implementation(spclibs.logback.classic)
    }
    jsMain {
        implementation(projects.ui.ring)
        implementation(projects.visionforgeThreejs)
        implementation(npm("react-file-drop", "3.0.6"))
    }
}

kotlin {
    explicitApi = null
}

//kotlin {
//
//    sourceSets {
//        commonMain {
//            dependencies {
//                implementation(project(":visionforge-solid"))
//                implementation(project(":visionforge-gdml"))
//            }
//        }
//        jvmMain {
//            dependencies {
////                implementation(project(":visionforge-fx"))
//                implementation("ch.qos.logback:logback-classic:1.2.11")
//            }
//        }
//        jsMain {
//            dependencies {
//                implementation(project(":ui:ring"))
//                implementation(project(":visionforge-threejs"))
//                implementation(npm("react-file-drop", "3.0.6"))
//            }
//        }
//    }
//}

//val convertGdmlToJson by tasks.creating(JavaExec::class) {
//    group = "application"
//    classpath = sourceSets["main"].runtimeClasspath
//    mainClass.set("space.kscience.dataforge.vis.spatial.gdml.demo.SaveToJsonKt")
//}