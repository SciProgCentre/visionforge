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

package space.kscience.visionforge.editor

import javafx.scene.control.TreeItem
import javafx.scene.control.TreeSortMode
import javafx.scene.control.TreeTableView
import javafx.scene.layout.BorderPane
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.values.string
import space.kscience.visionforge.dfIconView
import tornadofx.*

public class MetaViewer(
    private val rootNode: FXMetaModel<Meta>,
    title: String = "Meta viewer"
) : Fragment(title, dfIconView) {

    public constructor(meta: Meta, title: String = "Meta viewer") : this(
        FXMetaModel.root(
            meta
        ), title = title
    )

    override val root: BorderPane = borderpane {
        center {
            treetableview<FXMetaModel<*>> {
                isShowRoot = false
                root = TreeItem(rootNode)
                populate {
                    val fxMeta = it.value
                    fxMeta.children
                }
                root.isExpanded = true
                sortMode = TreeSortMode.ALL_DESCENDANTS
                columnResizePolicy = TreeTableView.CONSTRAINED_RESIZE_POLICY
                column("Name", FXMetaModel<*>::title)
                column<FXMetaModel<*>, String>("Value") { cellDataFeatures ->
                    val item = cellDataFeatures.value.value
                    item.valueProperty.stringBinding { it?.string ?: "" }
                }
            }
        }
    }
}