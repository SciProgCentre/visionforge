import space.kscience.gradle.Maturity

plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose)
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
                api(compose.foundation)
                api(compose.runtime)
                api(compose.material)
                api(compose.materialIconsExtended)
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