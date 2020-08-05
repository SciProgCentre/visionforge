package hep.dataforge.vision

import hep.dataforge.meta.Config
import hep.dataforge.names.NameToken
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("group")
class SimpleVisualGroup : AbstractVisualGroup() {

    override var styleSheet: StyleSheet? = null

    //FIXME to be lifted to AbstractVisualGroup after https://github.com/Kotlin/kotlinx.serialization/issues/378 is fixed
    override var ownProperties: Config? = null

    @SerialName("children")
    private val _children = HashMap<NameToken, VisualObject>()
    override val children: Map<NameToken, VisualObject> get() = _children

    override fun removeChild(token: NameToken) {
        _children.remove(token)?.apply { parent = null }
    }

    override fun setChild(token: NameToken, child: VisualObject) {
        _children[token] = child
    }

    override fun createGroup(): SimpleVisualGroup = SimpleVisualGroup()
}