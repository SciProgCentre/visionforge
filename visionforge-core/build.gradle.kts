plugins {
    id("space.kscience.gradle.mpp")
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
        commonTest{
            dependencies{
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${space.kscience.gradle.KScienceVersions.coroutinesVersion}")
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
    maturity = space.kscience.gradle.Maturity.DEVELOPMENT
}