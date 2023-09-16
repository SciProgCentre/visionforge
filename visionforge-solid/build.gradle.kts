plugins {
    id("space.kscience.gradle.mpp")
}

val kmathVersion = "0.3.1"

kscience {
    jvm()
    js()
    native()
    useSerialization {
        json()
    }
    useCoroutines()
    dependencies {
        api("space.kscience:kmath-geometry:0.3.1")
        api(projects.visionforgeCore)
    }
    dependencies(jvmTest) {
        implementation(spclibs.logback.classic)
    }
}

readme {
    maturity = space.kscience.gradle.Maturity.DEVELOPMENT
}