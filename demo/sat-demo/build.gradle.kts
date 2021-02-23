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
}

application {
    mainClass.set("ru.mipt.npm.gradle.sat.SatServerKt")
}
