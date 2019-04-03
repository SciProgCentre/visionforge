package hep.dataforge.vis.spatial.gdml

import hep.dataforge.meta.Config

@DslMarker
annotation class GDMLApi

@GDMLApi
class GDML {
    private val defines = GDMLDefineContainer()
    private var solids = GDMLSolidContainer()

    fun define(block: GDMLDefineContainer.() -> Unit) {
        defines.apply(block).map
    }

    fun solids(block: GDMLSolidContainer.() -> Unit) {
        solids.apply(block).map
    }

    fun getPosition(ref: String) = defines.map[ref] as? GDMLPosition
    fun getRotation(ref: String) = defines.map[ref] as? GDMLRotation
    fun getSolid(ref:String) = solids.map[ref]
}

@GDMLApi
class GDMLDefineContainer {
    internal val map =  HashMap<String, GDMLDefine>()

    fun position(name: String, block: GDMLPosition.() -> Unit) {
        map[name] = GDMLPosition(Config()).apply(block).apply { this.pName = name }
    }

    fun rotation(name: String, block: GDMLRotation.() -> Unit) {
        map[name] = GDMLRotation(Config()).apply(block).apply { this.pName = name }
    }
}

@GDMLApi
class GDMLSolidContainer {
    internal val map = HashMap<String, GDMLSolid>()

    operator fun get(ref: String) = map[ref]

    fun box(name: String, block: GDMLBox.() -> Unit) {
        map[name] = GDMLBox.build(block).apply{this.pName = name}
    }
    fun tube(name: String, block: GDMLTube.() -> Unit){
        map[name] = GDMLTube.build(block).apply{this.pName = name}
    }
    fun xtru(name: String, block: GDMLXtru.() -> Unit) {
        map[name] = GDMLXtru.build(block).apply{this.pName = name}
    }
}