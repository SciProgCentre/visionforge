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
    }
}

readme{
    maturity = ru.mipt.npm.gradle.Maturity.DEVELOPMENT
}