plugins {
    id("ru.mipt.npm.gradle.mpp")
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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
            }
        }
    }
}

readme{
    maturity = ru.mipt.npm.gradle.Maturity.DEVELOPMENT
}