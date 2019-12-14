package hep.dataforge.vis.spatial.fx

import hep.dataforge.vis.spatial.Proxy
import javafx.scene.Node
import kotlin.reflect.KClass

class FXProxyFactory(val plugin: FX3DPlugin) :
    FX3DFactory<Proxy> {
    override val type: KClass<in Proxy> get() = Proxy::class

    override fun invoke(obj: Proxy, binding: DisplayObjectFXBinding): Node {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}