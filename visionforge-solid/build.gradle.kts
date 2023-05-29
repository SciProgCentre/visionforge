plugins {
    id("space.kscience.gradle.mpp")
}

kscience {
    jvm()
    js()
    native()
    useSerialization {
        json()
    }
    useCoroutines()
    dependencies {
        api(projects.visionforgeCore)
    }
    dependencies(jvmTest) {
        implementation(spclibs.logback.classic)
    }
}

readme {
    maturity = space.kscience.gradle.Maturity.DEVELOPMENT
}