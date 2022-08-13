import space.kscience.gradle.KScienceVersions

plugins {
    id("space.kscience.gradle.mpp")
}

kscience{
    useSerialization{
        json()
    }
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":visionforge-core"))
            }
        }
        commonTest{
            dependencies{
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${KScienceVersions.coroutinesVersion}")
            }
        }
        jvmTest{
            dependencies{
                implementation("ch.qos.logback:logback-classic:1.2.11")
            }
        }
    }
}

readme{
    maturity = space.kscience.gradle.Maturity.DEVELOPMENT
}