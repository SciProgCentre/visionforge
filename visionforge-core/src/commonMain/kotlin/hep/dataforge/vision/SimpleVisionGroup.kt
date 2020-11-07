package hep.dataforge.vision

import hep.dataforge.meta.Config
import hep.dataforge.names.NameToken
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("group")
public class SimpleVisionGroup : AbstractVisionGroup() {

    override var styleSheet: StyleSheet? = null

    @SerialName("children")
    private val _children = HashMap<NameToken, Vision>()
    override val children: Map<NameToken, Vision> get() = _children

    override fun removeChild(token: NameToken) {
        _children.remove(token)?.apply { parent = null }
    }

    override fun setChild(token: NameToken, child: Vision) {
        _children[token] = child
    }

    override fun createGroup(): SimpleVisionGroup = SimpleVisionGroup()
}