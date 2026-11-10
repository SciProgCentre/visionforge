plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.compose.compiler)
    alias(spclibs.plugins.compose.jb)
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
    }

    jsMain {
        api(projects.visionforgeComposeHtml)
        api("org.jetbrains.kotlin-wrappers:kotlin-js")
        api("io.github.vinceglb:filekit-core:0.14.2")
//        api(npm("file-saver", "2.0.5"))
//        api(npm("@types/file-saver", "2.0.7"))
        implementation(npm("three", "0.173.0"))
        implementation(npm("@types/three", "0.173.0"))
        implementation(npm("three-csg-ts", "3.2.0"))
        implementation(npm("meshline", "3.3.1"))
    }
}


