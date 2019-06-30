import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openjfx.gradle.JavaFXOptions

plugins {
    kotlin("jvm") 
    id("org.openjfx.javafxplugin")
}

dependencies {
    api(project(":dataforge-vis-common"))
    api("no.tornado:tornadofx:1.7.19")
    api("no.tornado:tornadofx-controlsfx:0.1")
}

configure<JavaFXOptions> {
    modules("javafx.controls")
}

tasks.withType<KotlinCompile> {
    kotlinOptions{
        jvmTarget = "1.8"
    }
}