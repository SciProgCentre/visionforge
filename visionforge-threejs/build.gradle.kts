plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose)
}

kotlin {
    explicitApi = org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Disabled
}

kscience {
    js {
        binaries.library()
    }

    commonMain {
        api(projects.visionforgeSolid)
        api(projects.visionforgeCompose)
    }

    jsMain {
        implementation(npm("three", "0.143.0"))
        implementation(npm("three-csg-ts", "3.1.13"))
        implementation(npm("three.meshline", "1.4.0"))
    }
}


