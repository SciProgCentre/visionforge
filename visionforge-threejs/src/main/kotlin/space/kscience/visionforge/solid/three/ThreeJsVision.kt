package space.kscience.visionforge.solid.three

import org.w3c.dom.url.URL
import space.kscience.visionforge.solid.SolidBase
import three.core.Object3D

/**
 * A custom visual object that has its own Three.js renderer
 */
public abstract class ThreeJsVision : SolidBase<ThreeJsVision>() {
    public abstract suspend fun render(three: ThreePlugin): Object3D
}

public class ThreeStlVision(val url: URL): ThreeJsVision(){
    override suspend fun render(three: ThreePlugin): Object3D {
//        suspendCoroutine {
//
//        }
//        STLLoader()

        TODO()
    }

}
