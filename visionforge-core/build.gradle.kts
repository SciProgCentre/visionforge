plugins {
    id("ru.mipt.npm.gradle.mpp")
}

val dataforgeVersion: String by rootProject.extra

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api("space.kscience:dataforge-context:$dataforgeVersion")
                api(npmlibs.kotlinx.html)
                api("org.jetbrains.kotlin-wrappers:kotlin-css")
            }
        }
        jsMain {
            dependencies {
                api("org.jetbrains.kotlin-wrappers:kotlin-extensions")
            }
        }
    }
}

kscience{
    useSerialization{
        json()
    }
}

readme{
    maturity = ru.mipt.npm.gradle.Maturity.DEVELOPMENT
}