package hep.dataforge.vision

import hep.dataforge.names.NameToken
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("vision.group")
public class SimpleVisionGroup : AbstractVisionGroup() {
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