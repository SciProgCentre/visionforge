package hep.dataforge.vis

import kotlin.browser.document
import kotlin.dom.hasClass

external val module: Module

external interface Module {
    val hot: Hot?
}

external interface Hot {
    val data: dynamic

    fun accept()
    fun accept(dependency: String, callback: () -> Unit)
    fun accept(dependencies: Array<String>, callback: (updated: Array<String>) -> Unit)

    fun dispose(callback: (data: dynamic) -> Unit)
}

external fun require(name: String): dynamic

abstract class ApplicationBase {
    open val stateKeys: List<String> get() = emptyList()

    abstract fun start(state: Map<String, Any>)
    open fun dispose(): Map<String, Any> = emptyMap()
}

fun startApplication(builder: () -> ApplicationBase) {
    fun start(state: dynamic): ApplicationBase? {
        return if (document.body?.hasClass("testApp") == true) {
            val application = builder()

            @Suppress("UnsafeCastFromDynamic")
            application.start(state?.appState ?: emptyMap())

            application
        } else {
            null
        }
    }

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