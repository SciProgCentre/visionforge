package space.kscience.visionforge.demo.factory

import kotlinx.coroutines.runBlocking
import space.kscience.visionforge.server.close
import java.net.ServerSocket
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FactoryServerTest {

    private fun findFreePort(): Int {
        ServerSocket(0).use { socket ->
            return socket.localPort
        }
    }

    @Test
    fun testServerEndpoints() = runBlocking {
        val port = findFreePort()
        val server = FactoryServer.startServer(port)
        val client = HttpClient.newHttpClient()

        try {
            // Test root visionPage HTML
            val pageRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:$port/"))
                .GET()
                .build()
            val pageResponse = client.send(pageRequest, HttpResponse.BodyHandlers.ofString())
            assertEquals(200, pageResponse.statusCode())
            val html = pageResponse.body()
            assertTrue(html.contains("Отделение мокрой очистки газа"))
            assertTrue(html.contains("DEXPI 2.0.1"))
            assertTrue(html.contains("ГОСТ 14202"))

            // Test switch to GOST14202 mode
            val modeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:$port/api/mode/gost14202"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build()
            val modeResponse = client.send(modeRequest, HttpResponse.BodyHandlers.ofString())
            assertEquals(200, modeResponse.statusCode())
            assertTrue(modeResponse.body().contains("GOST14202"))

            // Test telemetry toggle
            val telemetryRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:$port/api/telemetry/toggle"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build()
            val telemetryResponse = client.send(telemetryRequest, HttpResponse.BodyHandlers.ofString())
            assertEquals(200, telemetryResponse.statusCode())
            assertTrue(telemetryResponse.body().contains("running"))

            // Test line visibility toggle
            val lineRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:$port/api/line/2/toggle"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build()
            val lineResponse = client.send(lineRequest, HttpResponse.BodyHandlers.ofString())
            assertEquals(200, lineResponse.statusCode())
            assertTrue(lineResponse.body().contains("line"))
        } finally {
            server.close()
        }
    }
}
