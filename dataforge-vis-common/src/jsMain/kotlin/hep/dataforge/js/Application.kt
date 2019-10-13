package hep.dataforge.js

import kotlin.browser.document
import kotlin.dom.hasClass

external val module: Module

external interface Hot {
    val data: dynamic

    fun accept()
    fun accept(dependency: String, callback: () -> Unit)
    fun accept(dependencies: Array<String>, callback: (updated: Array<String>) -> Unit)

    fun dispose(callback: (data: dynamic) -> Unit)
}

external interface Module {
    val hot: Hot?
}

/**
 * Base interface for applications.
 *
 * Base interface for applications supporting Hot Module Replacement (HMR).
 */
interface Application {
    /**
     * Starting point for an application.
     * @param state Initial state between Hot Module Replacement (HMR).
     */
    fun start(state: Map<String, Any>)

    /**
     * Ending point for an application.
     * @return final state for Hot Module Replacement (HMR).
     */
    fun dispose(): Map<String, Any> = emptyMap()
}

fun startApplication(builder: () -> Application) {
    fun start(state: dynamic): Application? {
        return if (document.body?.hasClass("testApp") == true) {
            val application = builder()

            @Suppress("UnsafeCastFromDynamic")
            application.start(state?.appState ?: emptyMap())

            application
        } else {
            null
        }
    }

    var application: Application? = null

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