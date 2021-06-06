package space.kscience.visionforge.ring

import kotlinx.css.*
import react.*
import ringui.grid.ringCol
import ringui.grid.ringGrid
import ringui.grid.ringRow
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.names.Name
import space.kscience.visionforge.Vision
import space.kscience.visionforge.react.ThreeCanvasComponent
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import styled.css
import styled.styledDiv

public external interface ThreeWithControlsProps : RProps {
    public var context: Context
    public var vision: Vision?
    public var selected: Name?
}

@JsExport
public val ThreeViewWithControls: (props: ThreeWithControlsProps) -> dynamic =
    functionalComponent("ThreeViewWithControls") { props ->
        var selected by useState { props.selected }
        val onSelect: (Name?) -> Unit = {
            selected = it
        }
        val options = useMemo {
            Canvas3DOptions.invoke {
                this.onSelect = onSelect
            }
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
                                this.obj = props.vision as? Solid
                                this.selected = selected
                                this.options = options
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
                            ringThreeControls(options, props.vision, selected, onSelect)
                        }
                    }
                }
            }
        }
    }