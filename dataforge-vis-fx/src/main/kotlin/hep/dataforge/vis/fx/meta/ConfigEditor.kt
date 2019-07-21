/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hep.dataforge.vis.fx.meta

import hep.dataforge.context.Global
import hep.dataforge.descriptors.NodeDescriptor
import hep.dataforge.meta.Config
import hep.dataforge.names.NameToken
import hep.dataforge.vis.fx.dfIconView
import hep.dataforge.vis.fx.values.ValueChooser
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTreeTableCell
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Text
import org.controlsfx.glyphfont.Glyph
import tornadofx.*

/**
 * A configuration editor fragment
 *
 * @author Alexander Nozik
 */
class ConfigEditor(
    val rootNode: FXMetaNode<Config>,
    val allowNew: Boolean = true,
    title: String = "Configuration editor"
) : Fragment(title = title, icon = dfIconView) {

    constructor(config: Config, descriptor: NodeDescriptor, title: String = "Configuration editor") :
            this(FXMeta.root(config, descriptor = descriptor), title = title)

    override val root = borderpane {
        center = treetableview<FXMeta<Config>> {
            root = TreeItem(rootNode)
            root.isExpanded = true
            sortMode = TreeSortMode.ALL_DESCENDANTS
            columnResizePolicy = TreeTableView.CONSTRAINED_RESIZE_POLICY
            populate {
                when (val fxMeta = it.value) {
                    is FXMetaNode -> {
                        fxMeta.children
                    }
                    is FXMetaValue -> null
                }
            }
            column("Name", FXMeta<Config>::name) {
                setCellFactory {
                    object : TextFieldTreeTableCell<FXMeta<Config>, NameToken>() {
                        override fun updateItem(item: NameToken?, empty: Boolean) {
                            super.updateItem(item, empty)
                            contextMenu?.items?.removeIf { it.text == "Remove" }
                            if (!empty) {
                                if (treeTableRow.item != null) {
                                    textFillProperty().bind(treeTableRow.item.hasValue.objectBinding {
                                        if (it == true) {
                                            Color.BLACK
                                        } else {
                                            Color.GRAY
                                        }
                                    })
                                    if (treeTableRow.treeItem.value.parent != null && treeTableRow.treeItem.value.hasValue.get()) {
                                        contextmenu {
                                            item("Remove") {
                                                action {
                                                    treeTableRow.item.remove()
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

            column("Value") { param: TreeTableColumn.CellDataFeatures<FXMeta<Config>, FXMeta<Config>> ->
                param.value.valueProperty()
            }.setCellFactory {
                ValueCell()
            }

            column("Description") { param: TreeTableColumn.CellDataFeatures<FXMeta<Config>, String> -> param.value.value.descriptionProperty }
                .setCellFactory { param: TreeTableColumn<FXMeta<Config>, String> ->
                    val cell = TreeTableCell<FXMeta<Config>, String>()
                    val text = Text()
                    cell.graphic = text
                    cell.prefHeight = Control.USE_COMPUTED_SIZE
                    text.wrappingWidthProperty().bind(param.widthProperty())
                    text.textProperty().bind(cell.itemProperty())
                    cell
                }
        }
    }

    private fun showNodeDialog(): String? {
        val dialog = TextInputDialog()
        dialog.title = "Node name selection"
        dialog.contentText = "Enter a name for new node: "
        dialog.headerText = null

        val result = dialog.showAndWait()
        return result.orElse(null)
    }

    private fun showValueDialog(): String? {
        val dialog = TextInputDialog()
        dialog.title = "Value name selection"
        dialog.contentText = "Enter a name for new value: "
        dialog.headerText = null

        val result = dialog.showAndWait()
        return result.orElse(null)
    }

    private inner class ValueCell : TreeTableCell<FXMeta<Config>, FXMeta<Config>?>() {

        public override fun updateItem(item: FXMeta<Config>?, empty: Boolean) {
            if (!empty) {
                if (item != null) {
                    when (item) {
                        is FXMetaValue<Config> -> {
                            text = null
                            val chooser = ValueChooser.build(Global, item.valueProperty, item.descriptor) {
                                item.set(it)
                            }
                            graphic = chooser.node
                        }
                        is FXMetaNode<Config> -> {
                            if (allowNew) {
                                text = null
                                graphic = hbox {
                                    button("node", Glyph("FontAwesome", "PLUS_CIRCLE")) {
                                        hgrow = Priority.ALWAYS
                                        maxWidth = Double.POSITIVE_INFINITY
                                        action {
                                            showNodeDialog()?.let {
                                                item.addNode(it)
                                            }
                                        }
                                    }
                                    button("value", Glyph("FontAwesome", "PLUS_SQUARE")) {
                                        hgrow = Priority.ALWAYS
                                        maxWidth = Double.POSITIVE_INFINITY
                                        action {
                                            showValueDialog()?.let {
                                                item.addValue(it)
                                            }
                                        }
                                    }
                                }
                            } else {
                                text = ""
                            }
                        }
                    }

                } else {
                    text = null
                    graphic = null
                }
            } else {
                text = null
                graphic = null
            }
        }

    }

    companion object {
        /**
         * The tag not to display node or value in configurator
         */
        const val NO_CONFIGURATOR_TAG = "nocfg"
    }
}
