package hep.dataforge.js

import kotlinx.browser.document
import kotlinx.dom.hasClass


public external val module: Module

public external interface Hot {
    public val data: dynamic

    public fun accept()
    public fun accept(dependency: String, callback: () -> Unit)
    public fun accept(dependencies: Array<String>, callback: (updated: Array<String>) -> Unit)

    public fun dispose(callback: (data: dynamic) -> Unit)
}

public external interface Module {
    public val hot: Hot?
}

/**
 * Base interface for applications.
 *
 * Base interface for applications supporting Hot Module Replacement (HMR).
 */
public interface Application {
    /**
     * Starting point for an application.
     * @param state Initial state between Hot Module Replacement (HMR).
     */
    public fun start(state: Map<String, Any>)

    /**
     * Ending point for an application.
     * @return final state for Hot Module Replacement (HMR).
     */
    public fun dispose(): Map<String, Any> = emptyMap()
}

public fun startApplication(builder: () -> Application) {
    fun start(state: dynamic): Application? {
        return if (document.body?.hasClass("application") == true) {
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