package hep.dataforge.vis.spatial.gdml

import hep.dataforge.meta.*

sealed class GDMLNode(override val config: Config) : Specification {
    var pName by string()
}

sealed class GDMLDefine(config: Config) : GDMLNode(config)

class GDMLPosition(config: Config) : GDMLDefine(config) {
    var x by number(0f).float
    var y by number(0f).float
    var z by number(0f).float
    var unit by string("cm")
}

class GDMLRotation(config: Config) : GDMLDefine(config) {
    var x by number(0f).float
    var y by number(0f).float
    var z by number(0f).float
    var unit by string("deg")
}



sealed class GDMLSolid(config: Config) : GDMLNode(config) {
    abstract val type: String
}

class GDMLBox(config: Config) : GDMLSolid(config) {
    override val type: String = "box"

    var pDx by number().double
    var pDy by number().double
    var pDz by number().double

    companion object : SpecificationCompanion<GDMLBox> {
        override fun wrap(config: Config): GDMLBox = GDMLBox(config)
    }
}

class GDMLTube(config: Config) : GDMLSolid(config) {
    override val type: String = "tube"

    var pRMin by number().double
    var pRMax by number().double
    var pDz by number().double
    var pSPhi by number().double
    var pDPhi by number().double

    companion object : SpecificationCompanion<GDMLTube> {
        override fun wrap(config: Config): GDMLTube = GDMLTube(config)
    }
}

class GDMLXtru(config: Config) : GDMLSolid(config) {
    override val type: String = "xtru"

    class TwoDimVertex(val x: Double, val y: Double)

    class Section(override val config: Config) : Specification {
        var zOrder by number().int
        var zPosition by number().double
        var xOffsset by number(0.0).double
        var yOffset by number(0.0).double
        var scalingFactor by number(1.0).double

        companion object : SpecificationCompanion<Section> {
            override fun wrap(config: Config): Section = Section(config)
        }
    }

    val verteces
        get() = config.getAll("twoDimVertex").values.map {
            val x = it.node["x"].double!!
            val y = it.node["y"].double!!
            TwoDimVertex(x, y)
        }

    val sections get() = config.getAll("section").values.map { Section(it.node!!) }

    fun vertex(x: Double, y: Double) {
        config.append("twoDimVertex", TwoDimVertex(x, y))
    }

    fun section(index: Int, z: Double, block: Section.() -> Unit) {
        config["section[$index]"] = Section.build(block).apply { zOrder = index; zPosition = z }
    }

    companion object : SpecificationCompanion<GDMLXtru> {
        override fun wrap(config: Config): GDMLXtru = GDMLXtru(config)
    }
}

class GDMLUnion(config: Config) : GDMLSolid(config) {
    override val type: String = "union"

    val first by node()
    val second by node()

    companion object : SpecificationCompanion<GDMLUnion> {
        override fun wrap(config: Config): GDMLUnion = GDMLUnion(config)
    }
}

