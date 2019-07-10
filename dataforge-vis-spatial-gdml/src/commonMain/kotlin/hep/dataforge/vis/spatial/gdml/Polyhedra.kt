package hep.dataforge.vis.spatial.gdml

import hep.dataforge.meta.Meta
import hep.dataforge.vis.common.DisplayLeaf
import hep.dataforge.vis.common.VisualObject
import hep.dataforge.vis.spatial.GeometryBuilder
import hep.dataforge.vis.spatial.Shape

class Polyhedra(parent: VisualObject?, meta: Meta) : DisplayLeaf(parent, meta), Shape {


    override fun <T : Any> toGeometry(geometryBuilder: GeometryBuilder<T>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}