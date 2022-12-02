package space.kscience.visionforge.examples

import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.routing.routing
import kotlinx.html.*
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.context.fetch
import space.kscience.visionforge.VisionManager
import space.kscience.visionforge.html.VisionPage
import space.kscience.visionforge.html.formFragment
import space.kscience.visionforge.onPropertyChange
import space.kscience.visionforge.server.close
import space.kscience.visionforge.server.openInBrowser
import space.kscience.visionforge.server.visionPage

fun main() {
    val visionManager = Global.fetch(VisionManager)

    val server = embeddedServer(CIO, 7777, "localhost") {

        routing {
            visionPage(visionManager, VisionPage.scriptHeader("js/visionforge-playground.js")) {
                val form = formFragment("form") {
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

                vision("form") { form }
                form.onPropertyChange {
                    println(this)
                }
            }
        }
    }.start(false)

    server.openInBrowser()

    while (readln() != "exit") {

    }

    server.close()
}