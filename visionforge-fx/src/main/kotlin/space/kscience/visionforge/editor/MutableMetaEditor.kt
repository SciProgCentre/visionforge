/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package space.kscience.visionforge.editor

import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTreeTableCell
import javafx.scene.layout.BorderPane
import javafx.scene.paint.Color
import javafx.scene.text.Text
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.meta.descriptors.MetaDescriptor
import space.kscience.visionforge.dfIconView
import tornadofx.*

/**
 * A Configuration editor fragment
 *
 * @author Alexander Nozik
 */
public class MutableMetaEditor(
    public val rootNode: FXMetaModel<MutableMeta>,
    public val allowNew: Boolean = true,
    title: String = "Configuration editor"
) : Fragment(title = title, icon = dfIconView) {
    //TODO replace parameters by properties

    public constructor(
        MutableMeta: MutableMeta,
        descriptor: MetaDescriptor?,
        title: String = "Configuration editor"
    ) :
            this(FXMetaModel.root(MutableMeta, descriptor = descriptor), title = title)

    override val root: BorderPane = borderpane {
        center = treetableview<FXMetaModel<MutableMeta>> {
            root = TreeItem(rootNode)
            root.isExpanded = true
            sortMode = TreeSortMode.ALL_DESCENDANTS
            columnResizePolicy = TreeTableView.CONSTRAINED_RESIZE_POLICY
            populate {
                it.value.children
            }
            column("Name", FXMetaModel<MutableMeta>::title) {
                setCellFactory {
                    object : TextFieldTreeTableCell<FXMetaModel<MutableMeta>, String>() {
                        override fun updateItem(item: String?, empty: Boolean) {
                            super.updateItem(item, empty)
                            contextMenu?.items?.removeIf { it.text == "Remove" }
                            val content = treeTableRow.item
                            if (!empty) {
                                if (treeTableRow.item != null) {
                                    textFillProperty().bind(content.existsProperty.objectBinding {
                                        if (it == true) {
                                            Color.BLACK
                                        } else {
                                            Color.GRAY
                                        }
                                    })
                                    if (content.exists) {
                                        contextmenu {
                                            item("Remove") {
                                                action {
                                                    content.remove()
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

            column("Value") { param: TreeTableColumn.CellDataFeatures<FXMetaModel<MutableMeta>, FXMetaModel<MutableMeta>> ->
                param.value.valueProperty()
            }.setCellFactory {
                ValueCell()
            }

            column("Description") { param: TreeTableColumn.CellDataFeatures<FXMetaModel<MutableMeta>, String> ->
                (param.value.value.descriptor?.info ?: "").observable()
            }.setCellFactory { param: TreeTableColumn<FXMetaModel<MutableMeta>, String> ->
                val cell = TreeTableCell<FXMetaModel<MutableMeta>, String>()
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

    private inner class ValueCell : TreeTableCell<FXMetaModel<MutableMeta>, FXMetaModel<MutableMeta>?>() {

        public override fun updateItem(item: FXMetaModel<MutableMeta>?, empty: Boolean) {
            if (!empty) {
                if (item != null) {
                    text = null
                    val chooser = ValueChooser.build(
                        Global,
                        item.valueProperty,
                        item.descriptor
                    ) {
                        item.setValue(it)
                    }
                    graphic = chooser.node
//                    when (item) {
//                        is FXMetaValue<MutableMeta> -> {
//                            text = null
//                            val chooser = ValueChooser.build(
//                                Global,
//                                item.valueProperty,
//                                item.descriptor
//                            ) {
//                                item.set(it)
//                            }
//                            graphic = chooser.node
//                        }
//                        is FXMetaNode<MutableMeta> -> {
//                            if (allowNew) {
//                                text = null
//                                graphic = HBox().apply {
//                                    val glyph: Node = FontAwesomeIconView(FontAwesomeIcon.PLUS_CIRCLE)
//                                    button("node", graphic = glyph) {
//                                        hgrow = Priority.ALWAYS
//                                        maxWidth = Double.POSITIVE_INFINITY
//                                        action {
//                                            showNodeDialog()?.let {
//                                                item.addNode(it)
//                                            }
//                                        }
//                                    }
//                                    button("value", graphic = FontAwesomeIconView(FontAwesomeIcon.PLUS_SQUARE)) {
//                                        hgrow = Priority.ALWAYS
//                                        maxWidth = Double.POSITIVE_INFINITY
//                                        action {
//                                            showValueDialog()?.let {
//                                                item.addValue(it)
//                                            }
//                                        }
//                                    }
//                                }
//                            } else {
//                                text = ""
//                            }
//                        }
//                    }

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

    public companion object {
        /**
         * The tag not to display node or value in MutableMetaurator
         */
        public const val NO_CONFIGURATOR_TAG: String = "nocfg"
    }
}
