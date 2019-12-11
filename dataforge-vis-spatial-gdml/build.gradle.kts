import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack

plugins {
    id("scientifik.mpp")
}

scientifik{
    withSerialization()
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":dataforge-vis-spatial"))
                api("scientifik:gdml:0.1.3")
            }
        }
        val jsMain by getting {
            dependencies {
                api(project(":dataforge-vis-spatial"))
                //api("kotlin.js.externals:kotlin-js-jquery:3.2.0-0")
            }
        }
    }
}

tasks{
    val jsBrowserWebpack by getting(KotlinWebpack::class) {
        sourceMaps = false
    }
}