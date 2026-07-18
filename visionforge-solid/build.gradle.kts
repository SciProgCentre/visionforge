plugins {
    id("space.kscience.gradle.mpp")
}

val kmathVersion = "0.4.2"

kscience {
    jvm()
    js()
    native()
    wasmJs()
    useSerialization {
        json()
    }
    useCoroutines()
    dependencies {
        api(libs.kmath.geometry)
        api(projects.visionforgeCore)
    }
    dependencies(jvmTest) {
        implementation(spclibs.logback.classic)
    }
}

readme {
    maturity = space.kscience.gradle.Maturity.DEVELOPMENT
}