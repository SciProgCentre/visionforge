import space.kscience.gradle.Maturity

plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose.compiler)
    alias(spclibs.plugins.compose.jb)
}

kscience {
    jvm()
//    wasm()
}

kotlin {
//    android()
    sourceSets {
        commonMain {
            dependencies {
                api(projects.visionforgeCore)
                api(libs.compose.foundation)
                api(libs.compose.runtime)
                api(libs.compose.material)
                api(libs.compose.materialIconsExtended)
            }
        }
        jvmMain {
            dependencies {
                implementation("com.eygraber:compose-color-picker:0.0.17")
            }
        }
    }
}

readme {
    maturity = Maturity.EXPERIMENTAL
}