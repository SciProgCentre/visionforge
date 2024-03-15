import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode


plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.ktor)
    application
}


kscience {
//    useSerialization {
//        json()
//    }
    jvm{
        withJava()
    }
    jvmMain{
        implementation("io.ktor:ktor-server-cio")
        implementation(projects.visionforgeThreejs.visionforgeThreejsServer)
        implementation(spclibs.logback.classic)
    }
}

group = "center.sciprog"

kotlin.explicitApi = ExplicitApiMode.Disabled

application {
    mainClass.set("ru.mipt.npm.sat.SatServerKt")
}
