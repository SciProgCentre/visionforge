plugins {
    id("space.kscience.gradle.jvm")
}

val dataforgeVersion: String by rootProject.extra
val fxVersion: String by rootProject.extra

kscience{
    useFx(space.kscience.gradle.FXModule.CONTROLS, version = fxVersion)
}

dependencies {
    api(project(":visionforge-solid"))
    api("no.tornado:tornadofx:1.7.20")
    api("org.fxyz3d:fxyz3d:0.5.4") {
        exclude(module = "slf4j-simple")
    }
    api("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:${space.kscience.gradle.KScienceVersions.coroutinesVersion}")
    implementation("eu.mihosoft.vrl.jcsg:jcsg:0.5.7") {
        exclude(module = "slf4j-simple")
    }
}

readme{
    maturity = space.kscience.gradle.Maturity.PROTOTYPE
}