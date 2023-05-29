plugins {
    id("space.kscience.gradle.jvm")
    application
}


kscience {
//    useSerialization {
//        json()
//    }
    dependencies{
        implementation(projects.visionforgeThreejs.visionforgeThreejsServer)
        implementation("ch.qos.logback:logback-classic:1.4.5")
    }
}

group = "ru.mipt.npm"

application {
    mainClass.set("ru.mipt.npm.sat.SatServerKt")
}
