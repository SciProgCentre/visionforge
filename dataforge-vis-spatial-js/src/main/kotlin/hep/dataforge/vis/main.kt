package hep.dataforge.vis

import hep.dataforge.vis.hmr.module
import hep.dataforge.vis.spatial.ThreeDemoApp
import kotlin.browser.document
import kotlin.dom.hasClass


abstract class ApplicationBase {
    abstract val stateKeys: List<String>

    abstract fun start(state: Map<String, Any>)
    abstract fun dispose(): Map<String, Any>
}


fun main() {
    var application: ApplicationBase? = null

    val state: dynamic = module.hot?.let { hot ->
        hot.accept()

        hot.dispose { data ->
            data.appState = application?.dispose()
            application = null
        }

        hot.data
    }

    if (document.body != null) {
        application = start(state)
    } else {
        application = null
        document.addEventListener("DOMContentLoaded", { application = start(state) })
    }
}

fun start(state: dynamic): ApplicationBase? {
    return if (document.body?.hasClass("testApp") == true) {
        val application = ThreeDemoApp()

        @Suppress("UnsafeCastFromDynamic")
        application.start(state?.appState ?: emptyMap())

        application
    } else {
        null
    }
}