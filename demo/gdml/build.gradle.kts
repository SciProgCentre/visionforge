plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose)
}

group = "demo"

kscience {
//    jvm()
    js {
        browser {
            binaries.executable()
            commonWebpackConfig{
                cssSupport{
                    enabled = true
                }
                scssSupport{
                    enabled = true
                }
                sourceMaps = true
            }
        }
    }
    dependencies {
        implementation(projects.visionforgeSolid)
        implementation(projects.visionforgeGdml)
    }
//    jvmMain {
////                implementation(project(":visionforge-fx"))
//        implementation(spclibs.logback.classic)
//    }
    jsMain {
        implementation(projects.visionforgeThreejs)
    }
}

kotlin {
    explicitApi = null
}


//val convertGdmlToJson by tasks.creating(JavaExec::class) {
//    group = "application"
//    classpath = sourceSets["main"].runtimeClasspath
//    mainClass.set("space.kscience.dataforge.vis.spatial.gdml.demo.SaveToJsonKt")
//}