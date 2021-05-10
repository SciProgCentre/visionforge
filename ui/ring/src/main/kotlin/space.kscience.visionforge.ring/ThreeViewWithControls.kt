package space.kscience.visionforge.ring

import kotlinx.css.*
import react.RProps
import react.child
import react.functionalComponent
import react.useState
import ringui.grid.ringCol
import ringui.grid.ringGrid
import ringui.grid.ringRow
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.react.ThreeCanvasComponent
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import space.kscience.visionforge.solid.three.ThreeCanvas
import styled.css
import styled.styledDiv

public external interface GdmlViewProps : RProps {
    public var context: Context
    public var rootVision: Vision?
    public var selected: Name?
}

@JsExport
public val ThreeViewWithControls: (props: GdmlViewProps) -> dynamic =
    functionalComponent<GdmlViewProps>("ThreeViewWithControls") { props ->
        var selected by useState { props.selected }
        var canvas: ThreeCanvas? by useState { null }

        val onSelect: (Name?) -> Unit = {
            selected = it
        }

        styledDiv {
            css {
                height = 100.pct
            }
            ringGrid {
                ringRow {
                    ringCol {
                        attrs {
                            xs = 12
                            sm = 12
                            md = 8
                            lg = 9
                        }
                        child(ThreeCanvasComponent) {
                            attrs {
                                this.context = props.context
                                this.obj = props.rootVision as? Solid
                                this.selected = selected
                                this.options = Canvas3DOptions.invoke {
                                    this.onSelect = onSelect
                                }
                                this.canvasCallback = {
                                    canvas = it
                                }
                            }
                        }

                    }
                    ringCol {
                        attrs {
                            xs = 12
                            sm = 12
                            md = 4
                            lg = 3
                        }
                        styledDiv {
                            css {
                                padding(top = 4.px)
                                //border(1.px, BorderStyle.solid, Color.lightGray)
                                height = 100.pct
                                overflowY = Overflow.auto
                            }
                            canvas?.let {
                                ringThreeControls(it, selected, onSelect)
                            }
                        }
                    }
                }
            }
        }
    }