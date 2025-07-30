plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.kotlin.jupyter.api)

}

description = "Common visionforge jupyter module"

kscience {
    jvm()
    js()
    dependencies {
        api(projects.visionforgeCore)
    }
    dependencies(jvmMain){
        api(projects.visionforgeServer)
        api(project.dependencies.platform(spclibs.ktor.bom))
        api("io.ktor:ktor-server-cio-jvm")
        api("io.ktor:ktor-server-websockets-jvm")
        api("io.ktor:ktor-server-cors-jvm")
    }
}

readme {
    maturity = space.kscience.gradle.Maturity.EXPERIMENTAL
}