package space.kscience.visionforge.solid

import eu.mihosoft.jcsg.PropertyStorage
import eu.mihosoft.jcsg.ext.quickhull3d.HullUtil
import eu.mihosoft.vvecmath.Vector3d
import javafx.scene.Node
import kotlin.reflect.KClass


public object FXConvexFactory : FX3DFactory<Convex> {
    override val type: KClass<in Convex> get() = Convex::class

    override fun invoke(obj: Convex, binding: VisualObjectFXBinding): Node {
        val hull = HullUtil.hull(
            obj.points.map { Vector3d.xyz(it.x.toDouble(), it.y.toDouble(), it.z.toDouble()) },
            PropertyStorage()
        )
        return hull.toNode()
    }

}