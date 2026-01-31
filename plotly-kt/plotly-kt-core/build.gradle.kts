plugins {
    id("space.kscience.gradle.mpp")
    alias(spclibs.plugins.kotlin.jupyter.api)
    `maven-publish`
}

val plotlyVersion by extra("2.35.3")

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
    }

    jsMain {
        api(npm("plotly.js", plotlyVersion))
    }

    nativeMain {
        implementation("com.squareup.okio:okio:3.3.0")
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