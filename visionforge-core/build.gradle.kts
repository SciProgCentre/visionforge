plugins {
    id("space.kscience.gradle.mpp")
}

val dataforgeVersion: String by rootProject.extra

kscience{
    jvm()
    js()
    native()
    dependencies {
        api("space.kscience:dataforge-context:$dataforgeVersion")
        api(spclibs.kotlinx.html)
//        api("org.jetbrains.kotlin-wrappers:kotlin-css")
    }
    testDependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${space.kscience.gradle.KScienceVersions.coroutinesVersion}")
    }
    dependencies(jsMain){
        api("org.jetbrains.kotlin-wrappers:kotlin-extensions")
    }
    useSerialization{
        json()
    }
}

readme{
    maturity = space.kscience.gradle.Maturity.DEVELOPMENT
}