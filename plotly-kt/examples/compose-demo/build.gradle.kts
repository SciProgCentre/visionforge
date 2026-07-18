plugins {
    kotlin("multiplatform")
    alias(spclibs.plugins.compose.compiler)
    alias(spclibs.plugins.compose.jb)
}

repositories {
    mavenCentral()
    maven("https://repo.kotlin.link")
    maven("https://jogamp.org/deployment/maven")
}

kotlin {
    jvm()
    jvmToolchain(21)
    sourceSets {
        jvmMain {
            dependencies {
                implementation(projects.plotlyKt.plotlyKtServer)
                api("io.ktor:ktor-server-cio")

                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material)
                implementation(compose.desktop.currentOs)
                implementation("io.github.kevinnzou:compose-webview-multiplatform:1.9.40")
                implementation(spclibs.logback.classic)
            }
        }
    }
}

compose {
    desktop {
        application {
            mainClass = "space.kscience.plotly.compose.AppKt"
        }
    }
}
