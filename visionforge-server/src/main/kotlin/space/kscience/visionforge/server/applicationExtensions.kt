package space.kscience.visionforge.server

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.Plugin
import io.ktor.server.application.install
import io.ktor.server.application.pluginOrNull
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.EngineConnectorBuilder
import io.ktor.server.engine.EngineConnectorConfig
import io.ktor.util.pipeline.Pipeline
import java.awt.Desktop
import java.net.URI


public fun <P : Pipeline<*, ApplicationCall>, B : Any, F : Any> P.require(
    plugin: Plugin<P, B, F>,
): F = pluginOrNull(plugin) ?: install(plugin)


/**
 * Connect to a given Ktor server using browser
 */
public fun ApplicationEngine.openInBrowser() {
    val connector = environment.connectors.first()
    val uri = URI("http", null, connector.host, connector.port, null, null, null)
    Desktop.getDesktop().browse(uri)
}

/**
 * Stop the server with default timeouts
 */
public fun ApplicationEngine.close(): Unit = stop(1000, 5000)


public fun EngineConnectorConfig(host: String, port: Int): EngineConnectorConfig = EngineConnectorBuilder().apply {
    this.host = host
    this.port = port
}