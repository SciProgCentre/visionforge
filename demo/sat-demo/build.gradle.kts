import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode


plugins {
    id("space.kscience.gradle.mpp")
//    alias(spclibs.plugins.ktor)
}


group = "center.sciprog"

kscience {
//    useSerialization {
//        json()
//    }
    jvm {
        application("ru.mipt.npm.sat.SatServerKt")
    }
    jvmMain {
        implementation("io.ktor:ktor-server-cio")
        implementation(projects.visionforgeThreejs.visionforgeThreejsServer)
        implementation(spclibs.logback.classic)
    }
}

kotlin.explicitApi = ExplicitApiMode.Disabled