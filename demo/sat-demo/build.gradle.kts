plugins {
    id("space.kscience.gradle.jvm")
    application
}


kscience {
//    useSerialization {
//        json()
//    }
    useKtor()
    dependencies{
        implementation("io.ktor:ktor-server-cio")
        implementation(projects.visionforgeThreejs.visionforgeThreejsServer)
        implementation(spclibs.logback.classic)
    }
}

group = "center.sciprog"

application {
    mainClass.set("ru.mipt.npm.sat.SatServerKt")
}
