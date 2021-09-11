package space.kscience.visionforge.examples

import ru.mipt.npm.root.DGeoManager
import ru.mipt.npm.root.serialization.TGeoManager
import ru.mipt.npm.root.toSolid
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.isLeaf
import space.kscience.dataforge.values.string
import space.kscience.visionforge.solid.Solids
import java.nio.file.Paths
import kotlin.io.path.writeText


private fun Meta.countTypes(): Sequence<String> = sequence {
    if (!isLeaf) {
        get("_typename")?.value?.let { yield(it.string) }
        items.forEach { yieldAll(it.value.countTypes()) }
    }
}

fun main() {
    val context = Context {
        plugin(Solids)
    }

    val string = TGeoManager::class.java.getResourceAsStream("/root/BM@N.root.json")!!
        .readAllBytes().decodeToString()

    val geo = DGeoManager.parse(string)


    val sizes = geo.meta.countTypes().groupBy { it }.mapValues { it.value.size }
    sizes.forEach {
        println(it)
    }


    val solid = geo.toSolid()

    Paths.get("BM@N.vf.json").writeText(Solids.encodeToString(solid))
    //println(Solids.encodeToString(solid))

    context.makeVisionFile {
        vision("canvas") {
            solid
       }
    }
}



/*            SolidGroup {
                set(
                    "Coil",
                    solid.getPrototype("Coil".asName())!!.apply {
                        parent = null
                    }
                )
               *//* group("Shade") {
                    y = 200
                    color("red")
                    coneSurface(
                        bottomOuterRadius = 135,
                        bottomInnerRadius = 25,
                        height = 50,
                        topOuterRadius = 135,
                        topInnerRadius = 25,
                        angle = 1.5707964
                    ) {
                        position = Point3D(79.6, 0, -122.1)
                        rotation = Point3D(-1.5707964, 0, 0)
                    }
                    coneSurface(
                        bottomOuterRadius = 135,
                        bottomInnerRadius = 25,
                        height = 50,
                        topOuterRadius = 135,
                        topInnerRadius = 25,
                        angle = 1.5707964
                    ) {
                        position = Point3D(-79.6, 0, -122.1)
                        rotation = Point3D(1.5707964, 0, -3.1415927)
                    }
                    coneSurface(
                        bottomOuterRadius = 135,
                        bottomInnerRadius = 25,
                        height = 50,
                        topOuterRadius = 135,
                        topInnerRadius = 25,
                        angle = 1.5707964
                    ) {
                        position = Point3D(79.6, 0, 122.1)
                        rotation = Point3D(1.5707964, 0, 0)
                    }
                    coneSurface(
                        bottomOuterRadius = 135,
                        bottomInnerRadius = 25,
                        height = 50,
                        topOuterRadius = 135,
                        topInnerRadius = 25,
                        angle = 1.5707964
                    ) {
                        position = Point3D(-79.6, 0, 122.1)
                        rotation = Point3D(-1.5707964, 0, -3.1415927)
                    }
                }*//*
            }*/
