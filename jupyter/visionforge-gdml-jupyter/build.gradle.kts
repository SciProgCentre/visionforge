plugins {
    id("ru.mipt.npm.gradle.jvm")
    kotlin("jupyter.api")
}

description = "Jupyter api artifact for GDML rendering"

dependencies {
    implementation(project(":visionforge-gdml"))
    implementation(project(":visionforge-threejs:visionforge-threejs-server"))
}

readme{
    maturity = ru.mipt.npm.gradle.Maturity.EXPERIMENTAL
}

