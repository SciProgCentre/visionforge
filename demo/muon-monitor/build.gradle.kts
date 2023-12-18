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
        jvmConfig = { withJava() },
        jsConfig = { useCommonJs() }
    )

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
        implementation(projects.visionforgeThreejs)
        //implementation(devNpm("webpack-bundle-analyzer", "4.4.0"))
    }
}

kotlin.explicitApi = null

application {
    mainClass.set("ru.mipt.npm.muon.monitor.server.MMServerKt")
}

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