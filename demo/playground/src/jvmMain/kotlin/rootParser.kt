package space.kscience.visionforge.examples

import ru.mipt.npm.root.DGeoManager
import ru.mipt.npm.root.rootGeo
import ru.mipt.npm.root.serialization.TGeoManager
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.isLeaf
import space.kscience.dataforge.meta.string
import space.kscience.visionforge.Colors
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.solid.Solids
import space.kscience.visionforge.solid.ambientLight
import space.kscience.visionforge.solid.set
import space.kscience.visionforge.solid.solid
import java.util.zip.ZipInputStream
import kotlin.io.path.Path
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

    makeVisionFile(path = Path("data/output.html"), resourceLocation = ResourceLocation.EMBED) {
        vision("canvas") {
            requirePlugin(Solids)
            solid {
                ambientLight {
                    color.set(Colors.white)
                }
                rootGeo(geo,"BM@N", maxLayer = 3, ignoreRootColors = true).also {
                    Path("data/BM@N.vf.json").writeText(Solids.encodeToString(it))
                }
            }
        }
    }
}