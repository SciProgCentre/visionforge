package space.kscience.visionforge

import kotlinx.browser.document
import kotlinx.coroutines.CoroutineScope
import org.w3c.dom.Document
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


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
public interface Application: CoroutineScope {

    override val coroutineContext: CoroutineContext get() = EmptyCoroutineContext

    /**
     * Starting point for an application.
     * @param state Initial state between Hot Module Replacement (HMR).
     */
    public fun start(document: Document, state: Map<String, Any>)

    /**
     * Ending point for an application.
     * @return final state for Hot Module Replacement (HMR).
     */
    public fun dispose(): Map<String, Any> = emptyMap()
}

public fun startApplication(builder: () -> Application) {
    fun start(document: Document, state: dynamic): Application{
        val application = builder()

        @Suppress("UnsafeCastFromDynamic")
        application.start(document, state?.appState ?: emptyMap())

        return application
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
        application = start(document, state)
    } else {
        application = null
        document.addEventListener("DOMContentLoaded", { application = start(document, state) })
    }
}