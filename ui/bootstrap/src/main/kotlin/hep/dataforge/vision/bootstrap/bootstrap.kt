package hep.dataforge.vision.bootstrap

public fun useBootstrap(){
    kotlinext.js.require("bootstrap/dist/css/bootstrap.min.css")
    kotlinext.js.require("bootstrap")
}

//public inline fun TagConsumer<HTMLElement>.card(title: String, crossinline block: TagConsumer<HTMLElement>.() -> Unit) {
//    div("card w-100") {
//        div("card-body") {
//            h3(classes = "card-title") { +title }
//            block()
//        }
//    }
//}

//public typealias SectionsBuilder = MutableList<Pair<String, DIV.() -> Unit>>
//
//public fun SectionsBuilder.entry(title: String, builder: DIV.() -> Unit) {
//    add(title to builder)
//}


//public fun TagConsumer<HTMLElement>.accordion(id: String, elements: List<Pair<String, DIV.() -> Unit>>) {
//    div("container-fluid") {
//        div("accordion") {
//            this.id = id
//            elements.forEachIndexed { index, (title, builder) ->
//                val headerID = "${id}-${index}-heading"
//                val collapseID = "${id}-${index}-collapse"
//                div("card") {
//                    div("card-header") {
//                        this.id = headerID
//                        h5("mb-0") {
//                            button(classes = "btn btn-link collapsed", type = ButtonType.button) {
//                                attributes["data-toggle"] = "collapse"
//                                attributes["data-target"] = "#$collapseID"
//                                attributes["aria-expanded"] = "false"
//                                attributes["aria-controls"] = collapseID
//                                +title
//                            }
//                        }
//                    }
//                    div("collapse") {
//                        this.id = collapseID
//                        attributes["aria-labelledby"] = headerID
//                        attributes["data-parent"] = "#$id"
//                        div("card-body", block = builder)
//                    }
//                }
//            }
//        }
//    }
//}


//public fun TagConsumer<HTMLElement>.accordion(id: String, builder: AccordionBuilder.() -> Unit) {
//    val list = ArrayList<Pair<String, DIV.() -> Unit>>().apply(builder)
//    accordion(id, list)
//}

//public fun Element.displayCanvasControls(canvas: ThreeCanvas, block: TagConsumer<HTMLElement>.() -> Unit = {}) {
//    clear()
//    append {
//        accordion("controls") {
//            entry("Settings") {
//                div("row") {
//                    div("col-2") {
//                        label("checkbox-inline") {
//                            input(type = InputType.checkBox) {
//                                checked = canvas.axes.visible
//                                onChangeFunction = {
//                                    canvas.axes.visible = checked
//                                }
//                            }
//                            +"Axes"
//                        }
//                    }
//                    div("col-1") {
//                        button {
//                            +"Export"
//                            onClickFunction = {
//                                val json = (canvas.content as? SolidGroup)?.let { group ->
//                                    val visionManager = canvas.context.plugins.fetch(SolidManager).visionManager
//                                    visionManager.encodeToString(group)
//                                }
//                                if (json != null) {
//                                    saveData(it, "object.json", "text/json") {
//                                        json
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//            entry("Layers") {
//                div("row") {
//                    (0..11).forEach { layer ->
//                        div("col-1") {
//                            label { +layer.toString() }
//                            input(type = InputType.checkBox) {
//                                if (layer == 0) {
//                                    checked = true
//                                }
//                                onChangeFunction = {
//                                    if (checked) {
//                                        canvas.camera.layers.enable(layer)
//                                    } else {
//                                        canvas.camera.layers.disable(layer)
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        block()
//    }
//}