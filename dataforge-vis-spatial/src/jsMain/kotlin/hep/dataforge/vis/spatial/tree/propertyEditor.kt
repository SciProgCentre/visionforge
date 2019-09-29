package hep.dataforge.vis.spatial.tree

import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.spatial.color
import hep.dataforge.vis.spatial.opacity
import hep.dataforge.vis.spatial.visible
import kotlinx.html.InputType
import kotlinx.html.dom.append
import kotlinx.html.js.*
import org.w3c.dom.Element
import kotlin.dom.clear

fun Element.propertyEditor(item: VisualObject?, name: String?) {
    clear()
    if (item != null) {
        append {
            div("card") {
                div("card-body") {
                    h3(classes = "card-title") { +(name ?: "") }
                    form {
                        div("form-group row") {
                            label("col-form-label col-4") {
                                +"Color: "
                            }
                            input(InputType.color, classes = "form-control col-8") {
                                value = item.color ?: "#ffffff"
                            }.apply {
                                onInputFunction = {
                                    item.color = value
                                }
                            }
                        }
                        div("form-group row") {
                            label("col-form-label col-4") {
                                +"Opacity: "
                            }
                            input(InputType.range, classes = "form-control col-8") {
                                min = "0.0"
                                max = "1.0"
                                step = "0.1"
                                value = item.opacity.toString()
                            }.apply {
                                onInputFunction = {
                                    item.opacity = value.toDouble()
                                }
                            }
                        }
                        div("form-group row") {
                            label("col-form-label col-4") { +"Visible: " }
                            div("col-8") {
                                div("form-check") {
                                    input(InputType.checkBox, classes = "form-check-input").apply {
                                        this.checked = item.visible ?: true
                                        onInputFunction = {
                                            item.visible = checked
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}
