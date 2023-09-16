plugins {
    id("space.kscience.gradle.mpp")
}

description = "Common visionforge jupyter module"

kscience {
    useKtor()
    jvm()
    js()
    jupyterLibrary()
    dependencies {
        api(projects.visionforgeCore)
    }
    dependencies(jvmMain){
        api("io.ktor:ktor-server-cio-jvm")
        api("io.ktor:ktor-server-websockets-jvm")
        api("io.ktor:ktor-server-cors-jvm")
        api(projects.visionforgeServer)
    }
}


readme {
    maturity = space.kscience.gradle.Maturity.EXPERIMENTAL
}