import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openjfx.gradle.JavaFXOptions

plugins {
    kotlin("jvm")
    id("org.openjfx.javafxplugin")
}

dependencies {
    api(project(":dataforge-vis-spatial"))
    api("no.tornado:tornadofx:1.7.18")
    implementation("org.fxyz3d:fxyz3d:0.4.0")
}

configure<JavaFXOptions> {
    modules("javafx.controls")
}

tasks.withType<KotlinCompile> {
    kotlinOptions{
        jvmTarget = "1.8"
    }
}