package space.kscience.visionforge.solid.three

import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextBaseline
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.MIDDLE
import space.kscience.visionforge.solid.SolidLabel
import space.kscience.visionforge.solid.SolidMaterial
import space.kscience.visionforge.solid.three.ThreeCanvas.Companion.DO_NOT_HIGHLIGHT_TAG
import three.DoubleSide
import three.core.Object3D
import three.geometries.PlaneGeometry
import three.materials.MeshBasicMaterial
import three.objects.Mesh
import three.textures.Texture
import kotlin.reflect.KClass

/**
 * Using example from http://stemkoski.github.io/Three.js/Texture-From-Canvas.html
 */
public object ThreeCanvasLabelFactory : ThreeFactory<SolidLabel> {
    override val type: KClass<in SolidLabel> get() = SolidLabel::class

    override fun build(three: ThreePlugin, vision: SolidLabel, observe: Boolean): Object3D {
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        canvas.getContext("2d").apply {
            this as CanvasRenderingContext2D
            font = "Bold ${vision.fontSize}pt ${vision.fontFamily}"
            fillStyle = vision.properties.getValue(
                SolidMaterial.MATERIAL_COLOR_KEY,
                inherit = false,
                includeStyles = true
            )?.value ?: "black"
            textBaseline = CanvasTextBaseline.MIDDLE
            val metrics = measureText(vision.text)
            //canvas.width = metrics.width.toInt()
            fillText(vision.text, (canvas.width - metrics.width) / 2, 0.5 * canvas.height)
        }


        // canvas contents will be used for a texture
        val texture = Texture(canvas)
        texture.needsUpdate = true

        val material = MeshBasicMaterial().apply {
            map = texture
            side = DoubleSide
            transparent = true
        }

        val mesh = Mesh(
            PlaneGeometry(canvas.width, canvas.height),
            material
        )

        mesh.updatePosition(vision)

        mesh.userData[DO_NOT_HIGHLIGHT_TAG] = true
        return mesh
    }
}