plugins {
    id("space.kscience.gradle.mpp")
}

val dataforgeVersion: String by rootProject.extra

kscience {
    jvm()
    js()
    native()
    useCoroutines()
    dependencies {
        api("space.kscience:dataforge-context:$dataforgeVersion")
        api(spclibs.kotlinx.html)
        api(spclibs.atomicfu)
//        api("org.jetbrains.kotlin-wrappers:kotlin-css")
    }
    jsMain {
        api("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    }
    useSerialization {
        json()
    }
}

readme {
    maturity = space.kscience.gradle.Maturity.DEVELOPMENT
}