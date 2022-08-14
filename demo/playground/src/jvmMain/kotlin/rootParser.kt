package space.kscience.visionforge.examples

import ru.mipt.npm.root.DGeoManager
import ru.mipt.npm.root.rootGeo
import ru.mipt.npm.root.serialization.TGeoManager
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.isLeaf
import space.kscience.dataforge.meta.string
import space.kscience.visionforge.solid.Solids
import java.nio.file.Paths
import java.util.zip.ZipInputStream
import kotlin.io.path.writeText


private fun Meta.countTypes(): Sequence<String> = sequence {
    if (!isLeaf) {
        get("_typename")?.value?.let { yield(it.string) }
        items.forEach { yieldAll(it.value.countTypes()) }
    }
}

fun main() {
    val string = ZipInputStream(TGeoManager::class.java.getResourceAsStream("/root/BM@N_geometry.zip")!!).use {
        it.nextEntry
        it.readAllBytes().decodeToString()
    }

    val geo = DGeoManager.parse(string)


    val sizes = geo.meta.countTypes().groupBy { it }.mapValues { it.value.size }
    sizes.forEach {
        println(it)
    }

    val solid = Solids.rootGeo(geo)

    Paths.get("BM@N.vf.json").writeText(Solids.encodeToString(solid))
    //println(Solids.encodeToString(solid))

    makeVisionFile {
        vision("canvas") {
            requirePlugin(Solids)
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
