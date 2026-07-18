plugins {
    id("space.kscience.gradle.mpp")
    alias(libs.plugins.kotlin.jupyter.api)
    `maven-publish`
}

val plotlyVersion = "2.35.3"

//kotlin{
//    applyDefaultHierarchyTemplate()
//}

kscience {
    fullStack(bundleName = "js/plotly-kt.js")
    native()
    wasmJs()
    useSerialization()

    commonMain {
        api(projects.visionforgeCore)
        api(project.dependencies.platform(spclibs.kotlin.js.wrappers))
    }

    jsMain {
        api(npm("plotly.js", plotlyVersion))
    }

    nativeMain {
        implementation(libs.okio)
    }

    wasmJsMain {
        api(npm("plotly.js", plotlyVersion))
        api(libs.kotlinx.browser)
    }
}

kotlinJupyter {
    integrations {
        producer("space.kscience.plotly.PlotlyIntegration")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xcontext-parameters")
    }
}

readme {
    maturity = space.kscience.gradle.Maturity.DEVELOPMENT
}