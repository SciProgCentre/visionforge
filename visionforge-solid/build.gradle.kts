plugins {
    id("space.kscience.gradle.mpp")
}

val kmathVersion = "0.4.2"

kscience {
    jvm()
    js()
    native()
//    wasm()
    useSerialization {
        json()
    }
    useCoroutines()
    dependencies {
        api("space.kscience:kmath-geometry:$kmathVersion")
        api(projects.visionforgeCore)
    }
    dependencies(jvmTest) {
        implementation(spclibs.logback.classic)
    }
}

readme {
    maturity = space.kscience.gradle.Maturity.DEVELOPMENT
}