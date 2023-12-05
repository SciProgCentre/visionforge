plugins {
    id("space.kscience.gradle.mpp")
}

val ktorVersion: String by rootProject.extra

kscience {
    fullStack("js/visionforge-three.js") {
        commonWebpackConfig {
            cssSupport {
                enabled.set(false)
            }
        }
    }

    dependencies {
        api(projects.visionforgeSolid)
    }

    dependencies(jvmMain) {
        api(projects.visionforgeServer)
    }

    dependencies(jsMain) {
        api(projects.visionforgeThreejs)
        api(projects.ui.ring)
        compileOnly(npm("webpack-bundle-analyzer","4.5.0"))
    }
}