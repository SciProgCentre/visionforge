plugins {
    id("space.kscience.gradle.mpp")
}

kscience {
    jvm()
    js {
        binaries.library()
    }
    dependencies {
        api(projects.visionforgeSolid)
        api("space.kscience:gdml:0.5.0")
        implementation("com.github.h0tk3y.betterParse:better-parse:0.4.4")
    }
    dependencies(jvmTest) {
        implementation(spclibs.logback.classic)
    }
}