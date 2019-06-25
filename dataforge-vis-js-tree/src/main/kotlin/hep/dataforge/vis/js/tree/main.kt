package hep.dataforge.vis.js.tree

import hep.dataforge.vis.js.tree.demo.TreeDemoApp
import hep.dataforge.vis.js.tree.demo.module
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
        document.addEventListener("DOMContentLoaded", {
            application = start(state) }
        )
    }
}

fun start(state: dynamic): ApplicationBase? {
    return if (document.body?.hasClass("testApp") == true) {
        val application = TreeDemoApp()

        @Suppress("UnsafeCastFromDynamic")
        application.start(state?.appState ?: emptyMap<String, Any>())

        application
    } else {
        null
    }
}