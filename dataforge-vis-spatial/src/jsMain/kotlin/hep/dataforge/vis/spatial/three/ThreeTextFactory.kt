package hep.dataforge.vis.spatial.three

import hep.dataforge.vis.spatial.Label3D
import info.laht.threekt.DoubleSide
import info.laht.threekt.core.Object3D
import info.laht.threekt.geometries.PlaneGeometry
import info.laht.threekt.materials.MeshBasicMaterial
import info.laht.threekt.objects.Mesh
import info.laht.threekt.textures.Texture
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.browser.document
import kotlin.reflect.KClass

/**
 * Using example from http://stemkoski.github.io/Three.js/Texture-From-Canvas.html
 */
object ThreeTextFactory : ThreeFactory<Label3D> {
    override val type: KClass<in Label3D> get() = Label3D::class

    override fun invoke(obj: Label3D): Object3D {
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        val context = canvas.getContext("2d") as CanvasRenderingContext2D
        context.font = "${obj.fontSize}pt ${obj.fontFamily}"
        context.fillStyle = "rgba(255,0,0,0.95)"//obj.material?.color ?: "black"
        context.fillText(obj.text, 0.0, 0.0)

        // canvas contents will be used for a texture
        val texture = Texture(canvas)
        texture.needsUpdate = true

        val material = MeshBasicMaterial().apply {
            map = texture
            side = DoubleSide
        }
        material.transparent = true;

        val mesh = Mesh(
            PlaneGeometry(canvas.clientWidth, canvas.clientHeight),
            material
        )

        mesh.updatePosition(obj)

        return mesh
    }
}