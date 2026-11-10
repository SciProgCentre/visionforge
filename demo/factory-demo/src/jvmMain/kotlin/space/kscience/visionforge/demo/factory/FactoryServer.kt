package space.kscience.visionforge.demo.factory

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.cio.CIO
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.delay
import kotlinx.html.*
import org.slf4j.LoggerFactory
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.request
import space.kscience.dataforge.names.parseAsName
import space.kscience.visionforge.Colors
import space.kscience.visionforge.demo.factory.model.FactorySceneConfig
import space.kscience.visionforge.demo.factory.scene.MogSceneBuilder
import space.kscience.visionforge.demo.factory.styles.DisplayMode
import space.kscience.visionforge.demo.factory.styles.MogStateManager
import space.kscience.visionforge.demo.factory.telemetry.MogTelemetrySimulator
import space.kscience.visionforge.html.VisionPage
import space.kscience.visionforge.html.meta
import space.kscience.visionforge.server.close
import space.kscience.visionforge.server.openInBrowser
import space.kscience.visionforge.server.visionPage
import space.kscience.visionforge.solid.*
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import space.kscience.visionforge.three.threeJsHeader
import space.kscience.visionforge.visible
import kotlin.time.Duration.Companion.seconds

public object FactoryServer {
    private val logger = LoggerFactory.getLogger(FactoryServer::class.java)

    public fun startServer(port: Int = 7777): EmbeddedServer<*, *> {
        val context = Context("factory-demo") {
            plugin(Solids)
        }
        val solids = context.request(Solids)

        logger.info("Loading MOG scene configuration...")
        val config = FactorySceneConfig.loadFromResources()

        logger.info("Generating 3D Solid hierarchy for 3 technological lines...")
        val root = MogSceneBuilder.buildScene(config).apply {
            ambientLight {
                color(Colors.white)
                intensity = 0.2
            }
            pointLight(x = 50.0, y = -50.0, z = 100.0, "pointLight") {
                color(Colors.white)
                intensity = 20000.0
            }
        }

        val simulator = MogTelemetrySimulator(root)

        val server = embeddedServer(CIO, port = port) {
            routing {
                staticResources("", null, null)

                post("/api/mode/{mode}") {
                    val modeParam = call.parameters["mode"]?.uppercase() ?: "MATERIAL"
                    val mode = runCatching { DisplayMode.valueOf(modeParam) }.getOrDefault(DisplayMode.MATERIAL)
                    simulator.currentDisplayMode = mode
                    MogStateManager.applyDisplayMode(root, config, mode)
                    call.respondText("""{"status":"ok","mode":"$mode"}""", ContentType.Application.Json)
                }

                post("/api/telemetry/toggle") {
                    if (simulator.isRunning) {
                        simulator.stop()
                    } else {
                        simulator.start()
                    }
                    call.respondText("""{"status":"ok","running":${simulator.isRunning}}""", ContentType.Application.Json)
                }

                post("/api/line/{lineNum}/toggle") {
                    val lineNum = call.parameters["lineNum"]?.toIntOrNull() ?: 1
                    val lineVision = root.getVision("NMZ.CPNSK.MOG.Line$lineNum".parseAsName())
                    if (lineVision != null) {
                        val current = lineVision.visible ?: true
                        lineVision.visible = !current
                        call.respondText("""{"status":"ok","line":$lineNum,"visible":${!current}}""", ContentType.Application.Json)
                    } else {
                        call.respondText("""{"status":"error","message":"Line not found"}""", ContentType.Application.Json, HttpStatusCode.NotFound)
                    }
                }
            }

            visionPage(
                solids.visionManager,
                VisionPage.title("МОГ — 3D Цифровой Двойник"),
                VisionPage.threeJsHeader,
                VisionPage.styleSheetHeader("css/factory.css"),
            ) {
                div("factory-header") {
                    div("factory-title-block") {
                        h1 { +"Отделение мокрой очистки газа" }
                        p { +"3D Цифровой двойник — 3 технологические линии, 26 аппаратов, 26 трубопроводов" }
                        div("badges") {
                            span("badge") { +"DEXPI 2.0.1" }
                            span("badge") { +"ГОСТ 14202-69" }
                            span("badge") { +"OpenUSD / Three.js" }
                            span("badge") { +"VisionForge" }
                        }
                    }

                    div("factory-controls") {
                        div("btn-group") {
                            button(classes = "btn active") {
                                id = "btn-mode-mat"
                                onClick = "setMode('MATERIAL', this)"
                                +"Материалы"
                            }
                            button(classes = "btn") {
                                id = "btn-mode-gost"
                                onClick = "setMode('GOST14202', this)"
                                +"ГОСТ 14202"
                            }
                            button(classes = "btn") {
                                id = "btn-mode-state"
                                onClick = "setMode('STATE', this)"
                                +"Состояние (DT)"
                            }
                        }

                        div("btn-group") {
                            button(classes = "btn active") {
                                id = "btn-line-1"
                                onClick = "toggleLine(1, this)"
                                +"Линия 1"
                            }
                            button(classes = "btn active") {
                                id = "btn-line-2"
                                onClick = "toggleLine(2, this)"
                                +"Линия 2"
                            }
                            button(classes = "btn active") {
                                id = "btn-line-3"
                                onClick = "toggleLine(3, this)"
                                +"Линия 3"
                            }
                        }

                        button(classes = "btn-toggle") {
                            id = "btn-telemetry"
                            onClick = "toggleTelemetry(this)"
                            +"Симуляция телеметрии"
                        }
                    }
                }

                div("main-container") {
                    div("canvas-container") {
                        vision {
                            meta(Canvas3DOptions {
                                controls.enabled = true
                            })
                            root
                        }
                    }

                    div("legend-panel") {
                        id = "legend-panel"
                        div("legend-title") { id = "legend-title"; +"Режим: Реалистичные материалы" }
                        div {
                            id = "legend-content"
                            div("legend-item") { div("legend-color") { style = "background: #d4c28f" }; span { +"Стеклопластик (FRP)" } }
                            div("legend-item") { div("legend-color") { style = "background: #66707a" }; span { +"Футерованная сталь (LinedSteel)" } }
                            div("legend-item") { div("legend-color") { style = "background: #a8abb0" }; span { +"Титан (Ti)" } }
                            div("legend-item") { div("legend-color") { style = "background: #c2c4c9" }; span { +"Нержавеющая сталь (SS)" } }
                            div("legend-item") { div("legend-color") { style = "background: #214d8c" }; span { +"Насосы (PumpBlue)" } }
                            div("legend-item") { div("legend-color") { style = "background: #99968f" }; span { +"Бетон плиты (Concrete)" } }
                        }
                    }
                }

                script {
                    unsafe {
                        +"""
                        function setMode(mode, btn) {
                            document.querySelectorAll('#btn-mode-mat, #btn-mode-gost, #btn-mode-state').forEach(b => b.classList.remove('active'));
                            btn.classList.add('active');
                            fetch('/api/mode/' + mode, { method: 'POST' });
                            updateLegend(mode);
                        }
                        
                        function toggleLine(num, btn) {
                            btn.classList.toggle('active');
                            fetch('/api/line/' + num + '/toggle', { method: 'POST' });
                        }
                        
                        function toggleTelemetry(btn) {
                            fetch('/api/telemetry/toggle', { method: 'POST' })
                                .then(r => r.json())
                                .then(data => {
                                    if (data.running) {
                                        btn.classList.add('active');
                                        btn.innerText = 'Остановить симуляцию';
                                    } else {
                                        btn.classList.remove('active');
                                        btn.innerText = 'Симуляция телеметрии';
                                    }
                                });
                        }
                        
                        function updateLegend(mode) {
                            const title = document.getElementById('legend-title');
                            const content = document.getElementById('legend-content');
                            if (mode === 'GOST14202') {
                                title.innerText = 'ГОСТ 14202-69 Опознавательная окраска';
                                content.innerHTML = `
                                    <div class="legend-item"><div class="legend-color" style="background: #218c21"></div><span>Вода (Зелёный)</span></div>
                                    <div class="legend-item"><div class="legend-color" style="background: #cc1a1a"></div><span>Пар (Красный)</span></div>
                                    <div class="legend-item"><div class="legend-color" style="background: #1f54cc"></div><span>Воздух (Синий)</span></div>
                                    <div class="legend-item"><div class="legend-color" style="background: #fada26"></div><span>Газы негорючие (Жёлтый)</span></div>
                                    <div class="legend-item"><div class="legend-color" style="background: #ee610a"></div><span>Кислоты (Оранжевый)</span></div>
                                    <div class="legend-item"><div class="legend-color" style="background: #8033a6"></div><span>Щёлочи (Фиолетовый)</span></div>
                                `;
                            } else if (mode === 'STATE') {
                                title.innerText = 'Цифровой двойник — Состояния (DT)';
                                content.innerHTML = `
                                    <div class="legend-item"><div class="legend-color" style="background: #33b34d"></div><span>В работе (Run)</span></div>
                                    <div class="legend-item"><div class="legend-color" style="background: #808080"></div><span>Резерв (Standby)</span></div>
                                    <div class="legend-item"><div class="legend-color" style="background: #2680eb"></div><span>Промывка (Flush)</span></div>
                                    <div class="legend-item"><div class="legend-color" style="background: #e62626"></div><span>Авария / Останов (Fault)</span></div>
                                    <div class="legend-item"><div class="legend-color" style="background: #eb8026"></div><span>Техобслуживание (Maint)</span></div>
                                `;
                            } else {
                                title.innerText = 'Режим: Реалистичные материалы';
                                content.innerHTML = `
                                    <div class="legend-item"><div class="legend-color" style="background: #d4c28f"></div><span>Стеклопластик (FRP)</span></div>
                                    <div class="legend-item"><div class="legend-color" style="background: #66707a"></div><span>Футерованная сталь (LinedSteel)</span></div>
                                    <div class="legend-item"><div class="legend-color" style="background: #a8abb0"></div><span>Титан (Ti)</span></div>
                                    <div class="legend-item"><div class="legend-color" style="background: #c2c4c9"></div><span>Нержавеющая сталь (SS)</span></div>
                                    <div class="legend-item"><div class="legend-color" style="background: #214d8c"></div><span>Насосы (PumpBlue)</span></div>
                                    <div class="legend-item"><div class="legend-color" style="background: #99968f"></div><span>Бетон плиты (Concrete)</span></div>
                                `;
                            }
                        }
                        """.trimIndent()
                    }
                }
            }
        }

        server.start(wait = false)
        logger.info("Factory 3D Digital Twin server started on http://localhost:$port/")
        return server
    }
}

public suspend fun main(args: Array<String>) {
    val port = args.firstOrNull()?.toIntOrNull() ?: 7777
    val server = FactoryServer.startServer(port)
    server.openInBrowser()

    println("MOG Factory 3D Digital Twin Server is running on http://localhost:$port/")
    println("Enter 'exit' or press Ctrl+C to stop the server...")
    while (readlnOrNull() != "exit") {
        delay(1.seconds)
    }

    server.close()
}
