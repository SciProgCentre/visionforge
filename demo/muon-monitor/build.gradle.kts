plugins {
    id("space.kscience.gradle.mpp")
    application
}

group = "ru.mipt.npm"

val ktorVersion: String = spclibs.versions.ktor.get()

kscience {
    useCoroutines()
    useSerialization()
    useKtor()
    fullStack(
        "muon-monitor.js",
        development = true,
        jvmConfig = { withJava() },
        jsConfig = { useCommonJs() }
    ) {
        commonWebpackConfig {
            cssSupport {
                enabled.set(false)
            }
        }
    }

    commonMain {
        implementation(projects.visionforgeSolid)
    }
    jvmMain {
        implementation("org.apache.commons:commons-math3:3.6.1")
        implementation("io.ktor:ktor-server-cio:${ktorVersion}")
        implementation("io.ktor:ktor-server-content-negotiation:${ktorVersion}")
        implementation("io.ktor:ktor-serialization-kotlinx-json:${ktorVersion}")
        implementation("ch.qos.logback:logback-classic:1.2.11")
    }
    jsMain {
        implementation(projects.ui.ring)
        implementation(projects.visionforgeThreejs)
        //implementation(devNpm("webpack-bundle-analyzer", "4.4.0"))
    }
}

kotlin.explicitApi = null

application {
    mainClass.set("ru.mipt.npm.muon.monitor.server.MMServerKt")
}

//TODO ???
tasks.getByName("jsBrowserProductionWebpack").dependsOn("jsDevelopmentExecutableCompileSync")

//distributions {
//    main {
//        contents {
//            from("$buildDir/libs") {
//                rename("${rootProject.name}-jvm", rootProject.name)
//                into("lib")
//            }
//        }
//    }
//}