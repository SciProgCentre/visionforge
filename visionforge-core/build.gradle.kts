plugins {
    id("space.kscience.gradle.mpp")
}


kscience {
    jvm()
    js()
    native()
    wasmJs()
    useCoroutines()
    commonMain {
        api(libs.dataforge.context)
        api(project.dependencies.platform(spclibs.ktor.bom))
        api(spclibs.kotlinx.html)
    }

    commonTest {
        implementation(libs.turbine)
    }
    jsMain {
        api("org.jetbrains.kotlin-wrappers:kotlin-js")
    }
    useSerialization {
        json()
    }
}

readme {
    maturity = space.kscience.gradle.Maturity.DEVELOPMENT
}