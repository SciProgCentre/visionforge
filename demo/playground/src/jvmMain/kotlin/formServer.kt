package space.kscience.visionforge.examples

import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.resources
import io.ktor.server.routing.routing
import kotlinx.html.*
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.context.request
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.html.VisionOfHtmlForm
import space.kscience.visionforge.html.VisionPage
import space.kscience.visionforge.html.bindForm
import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.server.EngineConnectorConfig
import space.kscience.visionforge.server.close
import space.kscience.visionforge.server.openInBrowser
import space.kscience.visionforge.server.visionPage

fun main() {
    val visionManager = Global.request(VisionManager)


    val connector = EngineConnectorConfig("localhost", 7777)

    val server = embeddedServer(CIO, connector.port, connector.host) {

        routing {
            resources()
        }

        val form = VisionOfHtmlForm("form").apply {
            onPropertyChange(visionManager.context) {
                println(values)
            }
        }

        visionPage(
            connector,
            visionManager,
            VisionPage.scriptHeader("js/visionforge-playground.js"),
        ) {
            bindForm(form) {
                label {
                    htmlFor = "fname"
                    +"First name:"
                }
                br()
                input {
                    type = InputType.text
                    id = "fname"
                    name = "fname"
                    value = "John"
                }
                br()
                label {
                    htmlFor = "lname"
                    +"Last name:"
                }
                br()
                input {
                    type = InputType.text
                    id = "lname"
                    name = "lname"
                    value = "Doe"
                }
                br()
                br()
                input {
                    type = InputType.submit
                    value = "Submit"
                }
            }
            println(form.values)
            vision(form)
        }

    }.start(false)

    server.openInBrowser()

    while (readln() != "exit") {

    }

    server.close()
}