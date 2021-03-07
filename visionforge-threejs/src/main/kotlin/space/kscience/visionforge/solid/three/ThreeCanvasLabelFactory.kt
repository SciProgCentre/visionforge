package space.kscience.visionforge.solid.three

import info.laht.threekt.DoubleSide
import info.laht.threekt.core.Object3D
import info.laht.threekt.geometries.PlaneBufferGeometry
import info.laht.threekt.materials.MeshBasicMaterial
import info.laht.threekt.objects.Mesh
import info.laht.threekt.textures.Texture
import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextBaseline
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.MIDDLE
import space.kscience.visionforge.solid.SolidLabel
import space.kscience.visionforge.solid.color
import space.kscience.visionforge.solid.three.ThreeCanvas.Companion.DO_NOT_HIGHLIGHT_TAG
import kotlin.reflect.KClass

/**
 * Using example from http://stemkoski.github.io/Three.js/Texture-From-Canvas.html
 */
public object ThreeCanvasLabelFactory : ThreeFactory<SolidLabel> {
    override val type: KClass<in SolidLabel> get() = SolidLabel::class

    override fun invoke(three: ThreePlugin, obj: SolidLabel): Object3D {
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        val context = canvas.getContext("2d") as CanvasRenderingContext2D
        context.font = "Bold ${obj.fontSize}pt ${obj.fontFamily}"
        context.fillStyle = obj.color ?: "black"
        context.textBaseline = CanvasTextBaseline.MIDDLE
        val metrics = context.measureText(obj.text)
        //canvas.width = metrics.width.toInt()


        context.fillText(obj.text, (canvas.width - metrics.width) / 2, 0.5 * canvas.height)


        // canvas contents will be used for a texture
        val texture = Texture(canvas)
        texture.needsUpdate = true

        val material = MeshBasicMaterial().apply {
            map = texture
            side = DoubleSide
            transparent = true
        }

        val mesh = Mesh(
            PlaneBufferGeometry(canvas.width, canvas.height),
            material
        )

        mesh.updatePosition(obj)

        mesh.userData[DO_NOT_HIGHLIGHT_TAG] = true
        return mesh
    }
}