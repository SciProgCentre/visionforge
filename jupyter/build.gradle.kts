plugins {
    id("space.kscience.gradle.mpp")
}

description = "Common visionforge jupyter module"

kscience {
    jvm()
    js()
    jupyterLibrary()
    dependencies {
        api(projects.visionforgeCore)
    }
    dependencies(jvmMain){
        api(projects.visionforgeServer)
    }
}


readme {
    maturity = space.kscience.gradle.Maturity.EXPERIMENTAL
}