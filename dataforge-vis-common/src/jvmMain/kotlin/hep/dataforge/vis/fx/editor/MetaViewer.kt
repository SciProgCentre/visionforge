/*
 * Copyright  2018 Alexander Nozik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package hep.dataforge.vis.fx.editor

import hep.dataforge.meta.Meta
import hep.dataforge.vis.fx.dfIconView
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeSortMode
import javafx.scene.control.TreeTableView
import tornadofx.*

class MetaViewer(val rootNode: FXMetaNode<*>, title: String = "Meta viewer") : Fragment(title, dfIconView) {
    constructor(meta: Meta, title: String = "Meta viewer"): this(FXMeta.root(meta),title = title)

    override val root = borderpane {
        center {
            treetableview<FXMeta<*>> {
                isShowRoot = false
                root = TreeItem(rootNode)
                populate {
                    when (val fxMeta = it.value) {
                        is FXMetaNode -> {
                            fxMeta.children
                        }
                        is FXMetaValue -> null
                    }
                }
                root.isExpanded = true
                sortMode = TreeSortMode.ALL_DESCENDANTS
                columnResizePolicy = TreeTableView.CONSTRAINED_RESIZE_POLICY
                column("Name", FXMeta<*>::name)
                column<FXMeta<*>, String>("Value") { cellDataFeatures ->
                    when (val item = cellDataFeatures.value.value) {
                        is FXMetaValue -> item.valueProperty.stringBinding { it?.string ?: "" }
                        is FXMetaNode -> SimpleStringProperty("[node]")
                    }
                }
            }
        }
    }
}