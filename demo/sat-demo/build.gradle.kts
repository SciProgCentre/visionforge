plugins {
    id("space.kscience.gradle.jvm")
    application
}


kscience {
//    useSerialization {
//        json()
//    }
    application()
}

group = "ru.mipt.npm"

dependencies{
    implementation(project(":visionforge-threejs:visionforge-threejs-server"))
    implementation("ch.qos.logback:logback-classic:1.4.5")
}

application {
    mainClass.set("ru.mipt.npm.sat.SatServerKt")
}
