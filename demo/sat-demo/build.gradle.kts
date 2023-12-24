plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.ktor)
    application
}


kscience {
//    useSerialization {
//        json()
//    }
    jvm()
    jvmMain{
        implementation("io.ktor:ktor-server-cio")
        implementation(projects.visionforgeThreejs.visionforgeThreejsServer)
        implementation(spclibs.logback.classic)
    }
}

group = "center.sciprog"

application {
    mainClass.set("ru.mipt.npm.sat.SatServerKt")
}
