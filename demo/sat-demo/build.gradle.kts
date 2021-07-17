plugins {
    id("ru.mipt.npm.gradle.jvm")
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
    implementation("ch.qos.logback:logback-classic:1.2.3")
}

application {
    mainClass.set("ru.mipt.npm.sat.SatServerKt")
}
