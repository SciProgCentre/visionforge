package hep.dataforge.vis.spatial.gdml.demo

import hep.dataforge.vis.spatial.VisualObject3D
import hep.dataforge.vis.spatial.tree.JSVisualTree
import nl.adaptivity.js.util.asElement
import org.w3c.dom.Element

fun Element.appendFancyTree(root: VisualObject3D){
    val visualTree = JSVisualTree("world", root) {}
    val treeNode = visualTree.root
    treeNode.asElement()!!.id = "fancytree"
    appendChild(treeNode)
}