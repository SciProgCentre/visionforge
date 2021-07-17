plugins {
    kotlin("multiplatform")
    application
    id("org.openjfx.javafxplugin") version "0.0.9"
}

repositories {
    maven("https://repo.kotlin.link")
    mavenLocal()
    jcenter()
}

kotlin {
    jvm {
        withJava()
    }

    js(IR) {
        browser()
    }

    sourceSets{
        commonMain{
            dependencies {

            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("no.tornado:tornadofx:1.7.19")
                implementation(project(":visionforge-server"))
                implementation("ch.qos.logback:logback-classic:1.2.3")
            }
        }
    }

}

javafx {
    modules("javafx.web")
    version = "16"
}

application {
    mainClassName = "space.kscience.plotly.fx.PlotlyFXAppKt"
}


//
//val compileKotlin: KotlinCompile by tasks
//compileKotlin.kotlinOptions {
//    jvmTarget = "11"
//}
//val compileTestKotlin: KotlinCompile by tasks
//compileTestKotlin.kotlinOptions {
//    jvmTarget = "11"
//}