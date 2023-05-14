plugins {
    id("space.kscience.gradle.mpp")
}

kscience {
    jvm()
    js()
    useSerialization {
        json()
    }
    dependencies {
        api(projects.visionforgeCore)
    }
    testDependencies {
        implementation(spclibs.kotlinx.coroutines.test)
    }
    dependencies(jvmTest) {
        implementation(spclibs.logback.classic)
    }
}

readme {
    maturity = space.kscience.gradle.Maturity.DEVELOPMENT
}