package hep.dataforge.vis.spatial.gdml

import hep.dataforge.meta.Config

class GDML {
    private var defines: List<GDMLDefine> = emptyList()
    private var solids: Map<String, GDMLSolid> = emptyMap()

    fun define(block: GDMLDefineBuilder.() -> Unit) {
        defines = GDMLDefineBuilder().apply(block).defines
    }

    fun solids(block: GDMLSolidBuilder.() -> Unit) {
        solids = GDMLSolidBuilder().apply(block).solids
    }
}

class GDMLDefineBuilder {
    internal val defines = ArrayList<GDMLDefine>()

    fun position(block: GDMLPosition.() -> Unit) {
        defines.add(GDMLPosition(Config()).apply(block))
    }

    fun rotation(block: GDMLRotation.() -> Unit) {
        defines.add(GDMLRotation(Config()).apply(block))
    }
}

class GDMLSolidBuilder {
    internal val solids = HashMap<String, GDMLSolid>()

    private fun put(solid: GDMLSolid) {
        solids[solid.pName!!] = solid
    }

    fun box(block: GDMLBox.() -> Unit) = put(GDMLBox.build(block))
    fun tube(block: GDMLTube.() -> Unit) = put(GDMLTube.build(block))
    fun xtru(block: GDMLXtru.() -> Unit) = put(GDMLXtru.build(block))
}