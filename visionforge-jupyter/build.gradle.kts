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
        api("io.ktor:ktor-server-cio")
        api(projects.visionforgeServer)
    }
}


readme {
    maturity = space.kscience.gradle.Maturity.EXPERIMENTAL
}