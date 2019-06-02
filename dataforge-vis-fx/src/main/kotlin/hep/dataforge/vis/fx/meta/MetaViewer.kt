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

package hep.dataforge.vis.fx.meta

import hep.dataforge.meta.Meta
import hep.dataforge.meta.seal
import hep.dataforge.vis.fx.dfIconView
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeSortMode
import javafx.scene.control.TreeTableView
import tornadofx.*

open class MetaViewer(val meta: Meta, title: String = "Meta viewer") : Fragment(title, dfIconView) {
    override val root = borderpane {
        center {
            treetableview<FXMeta> {
                isShowRoot = false
                root = TreeItem(FXMeta.root(meta.seal()))
                populate {
                    when (val fxMeta = it.value) {
                        is FXMetaNode<*> -> {
                            fxMeta.children
                        }
                        is FXMetaValue -> null
                    }
                }
                root.isExpanded = true
                sortMode = TreeSortMode.ALL_DESCENDANTS
                columnResizePolicy = TreeTableView.CONSTRAINED_RESIZE_POLICY
                column("Name", FXMeta::name)
                column<FXMeta, String>("Value"){
                    when(val item = it.value.value){
                        is FXMetaValue -> item.valueProperty.stringBinding{it?.string?: ""}
                        is FXMetaNode<*> -> SimpleStringProperty("[node]")
                    }
                }
            }
        }
    }
}