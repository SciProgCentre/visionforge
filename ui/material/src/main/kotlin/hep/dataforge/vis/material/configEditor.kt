package hep.dataforge.vis.material

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.*
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import hep.dataforge.names.isEmpty
import hep.dataforge.names.plus
import hep.dataforge.vis.react.component
import hep.dataforge.vis.react.state
import kotlinx.css.Display
import kotlinx.css.display
import kotlinx.css.flexGrow
import kotlinx.css.flexShrink
import kotlinx.html.js.onClickFunction
import materialui.components.button.button
import materialui.components.grid.enums.GridAlignItems
import materialui.components.grid.enums.GridJustify
import materialui.components.grid.grid
import materialui.components.typography.typographyH6
import materialui.lab.components.treeItem.treeItem
import materialui.lab.components.treeView.treeView
import org.w3c.dom.Element
import org.w3c.dom.events.Event
import react.*
import react.dom.render
import react.dom.span
import styled.css
import styled.styledDiv

interface ConfigEditorProps : RProps {

    /**
     * Root config object - always non null
     */
    var root: Config

    /**
     * Full path to the displayed node in [root]. Could be empty
     */
    var name: Name

    /**
     * Root default
     */
    var default: Meta?

    /**
     * Root descriptor
     */
    var descriptor: NodeDescriptor?
}

private fun RBuilder.configEditorItem(
    root: Config,
    name: Name,
    descriptor: NodeDescriptor?,
    default: Meta?
) {
    val item = root[name]
    val descriptorItem: ItemDescriptor? = descriptor?.get(name)
    val defaultItem = default?.get(name)
    val actualItem: MetaItem<Meta>? = item ?: defaultItem ?: descriptorItem?.defaultItem()

    val token = name.last()?.toString() ?: "Properties"

    val removeClick: (Event) -> Unit = {
        root.remove(name)
    }

    treeItem {
        attrs {
            nodeId = name.toString()
            label {
                row {
                    attrs {
                        alignItems = GridAlignItems.stretch
                        justify = GridJustify.spaceBetween
                        spacing(1)
                    }
                    grid {
                        typographyH6 {
                            +token
                        }
                    }
                    if (actualItem is MetaItem.ValueItem) {
                        styledDiv {
                            css {
                                display = Display.flex
                                flexGrow = 1.0
                            }
                            valueChooser(root, name, actualItem.value, descriptorItem as? ValueDescriptor)
                        }
                    }
                    if (!name.isEmpty()) {
                        styledDiv {
                            css {
                                display = Display.flex
                                flexShrink = 1.0
                            }
                            button {
                                +"\u00D7"
                                attrs {
                                    if (item == null) {
                                        disabled = true
                                    } else {
                                        onClickFunction = removeClick
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (actualItem is MetaItem.NodeItem) {
            val keys = buildSet<NameToken> {
                (descriptorItem as? NodeDescriptor)?.items?.keys?.forEach {
                    add(NameToken(it))
                }
                item?.node?.items?.keys?.let { addAll(it) }
                defaultItem?.node?.items?.keys?.let { addAll(it) }
            }

            keys.forEach { token ->
                configEditorItem(root, name + token, descriptor, default)
            }
        }
    }
}

val ConfigEditor: FunctionalComponent<ConfigEditorProps> = component { props ->
    var kostyl by state { false }

    fun update() {
        kostyl = !kostyl
    }

    useEffectWithCleanup(listOf(props.root)) {
        props.root.onChange(this) { name, _, _ ->
            if (name == props.name) {
                update()
            }
        }
        return@useEffectWithCleanup { props.root.removeListener(this) }
    }

    treeView {
        attrs {
            defaultCollapseIcon {
                span {
                    +"-"
                }
                //child(ExpandMoreIcon::class) {}
            }//{<ExpandMoreIcon />}
            defaultExpandIcon {
                span {
                    +"+"
                }
                //child(ChevronRightIcon::class) {}
            }//{<ChevronRightIcon />}
            set("disableSelection", true)
        }
        configEditorItem(props.root, props.name, props.descriptor, props.default)
    }

}

fun RBuilder.configEditor(
    config: Config,
    name: Name = Name.EMPTY,
    descriptor: NodeDescriptor? = null,
    default: Meta? = null
) {
    child(ConfigEditor) {
        attrs {
            this.root = config
            this.name = name
            this.descriptor = descriptor
            this.default = default
        }
    }
}

fun Element.configEditor(config: Config, descriptor: NodeDescriptor? = null, default: Meta? = null) {
    render(this) {
        configEditor(config, Name.EMPTY, descriptor, default)
    }
}

fun RBuilder.configEditor(obj: Configurable, descriptor: NodeDescriptor? = obj.descriptor, default: Meta? = null) {
    configEditor(obj.config, Name.EMPTY, descriptor ?: obj.descriptor, default)
}
