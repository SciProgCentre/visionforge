package space.kscience.visionforge.ring

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.css.*
import react.*
import react.dom.div
import react.dom.span
import ringui.*
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.NameToken
import space.kscience.dataforge.names.isEmpty
import space.kscience.dataforge.names.length
import space.kscience.visionforge.*
import space.kscience.visionforge.react.ThreeCanvasComponent
import space.kscience.visionforge.react.flexColumn
import space.kscience.visionforge.react.flexRow
import space.kscience.visionforge.react.propertyEditor
import space.kscience.visionforge.solid.Solid
import space.kscience.visionforge.solid.SolidGroup
import space.kscience.visionforge.solid.specifications.Canvas3DOptions
import styled.css
import styled.styledDiv

public external interface ThreeCanvasWithControlsProps : Props {
    public var context: Context
    public var builderOfSolid: Deferred<Solid?>
    public var selected: Name?
    public var options: Canvas3DOptions?
    public var additionalTabs: Map<String, RBuilder.() -> Unit>?
}

public fun ThreeCanvasWithControlsProps.solid(block: SolidGroup.() -> Unit) {
    builderOfSolid = context.async {
        SolidGroup(block)
    }
}

public fun ThreeCanvasWithControlsProps.tab(title: String, block: RBuilder.() -> Unit) {
    additionalTabs = (additionalTabs ?: emptyMap()) + (title to block)
}


public fun RBuilder.nameCrumbs(name: Name?, link: (Name) -> Unit): Unit = styledDiv {
    div {
        Link {
            attrs {
                onClick = {
                    link(Name.EMPTY)
                }
            }
            +"\u2302"
        }

        if (name != null) {
            val tokens = ArrayList<NameToken>(name.length)
            name.tokens.forEach { token ->
                tokens.add(token)
                val fullName = Name(tokens.toList())
                span { +"." }
                Link {
                    +token.toString()
                    attrs {
                        onClick = {
                            console.log("Selected = $fullName")
                            link(fullName)
                        }
                    }
                }
            }
        }
    }

}

@JsExport
public val ThreeCanvasWithControls: FC<ThreeCanvasWithControlsProps> = fc("ThreeViewWithControls") { props ->
    var selected by useState { props.selected }
    var solid: Solid? by useState(null)

    useEffect {
        props.context.launch {
            solid = props.builderOfSolid.await().also {
                it?.setAsRoot(props.context.visionManager)
            }
        }
    }

    val onSelect: (Name?) -> Unit = {
        selected = it
    }

    val options = useMemo(props.options) {
        (props.options ?: Canvas3DOptions()).apply {
            this.onSelect = onSelect
        }
    }

    val selectedVision: Vision? = useMemo(props.builderOfSolid, selected) {
        selected?.let {
            when {
                it.isEmpty() -> solid
                else -> (solid as? VisionGroup)?.get(it)
            }
        }
    }


    flexRow {
        css {
            height = 100.pct
            width = 100.pct
            flexWrap = FlexWrap.wrap
            alignItems = Align.stretch
            alignContent = Align.stretch
        }

        flexColumn {
            css {
                height = 100.pct
                minWidth = 600.px
                flex(10.0, 1.0, FlexBasis("600px"))
                position = Position.relative
            }

            if (solid == null) {
                LoaderScreen {
                    attrs {
                        message = "Loading Three vision"
                    }
                }
            } else {
                child(ThreeCanvasComponent) {
                    attrs {
                        this.context = props.context
                        this.solid = solid
                        this.selected = selected
                        this.options = options
                    }
                }
            }

            selectedVision?.let { vision ->
                styledDiv {
                    css {
                        position = Position.absolute
                        top = 5.px
                        right = 5.px
                        width = 450.px
                    }
                    Island {
                        IslandHeader {
                            attrs {
                                border = true
                            }
                            nameCrumbs(selected) { selected = it }
                        }
                        IslandContent {
                            propertyEditor(
                                ownProperties = vision.meta,
                                allProperties = vision.computeProperties(),
                                descriptor = vision.descriptor,
                                key = selected
                            )
                        }
                    }
                }
            }
        }
        flexColumn {
            css {
                padding(4.px)
                minWidth = 400.px
                height = 100.pct
                overflowY = Overflow.auto
                flex(1.0, 10.0, FlexBasis("300px"))
            }
            ringThreeControls(options, solid, selected, onSelect, additionalTabs = props.additionalTabs)
        }
    }
}

