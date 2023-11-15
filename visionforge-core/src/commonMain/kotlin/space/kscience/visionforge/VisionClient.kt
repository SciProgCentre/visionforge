package space.kscience.visionforge

import kotlinx.coroutines.launch
import space.kscience.dataforge.context.Plugin
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MetaRepr
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.parseAsName

/**
 * A  feedback client that communicates with a server and provides ability to propagate events and changes back to the model
 */
public interface VisionClient: Plugin {
    public val visionManager: VisionManager

    public suspend fun sendEvent(targetName: Name, event: VisionEvent)

    public fun notifyPropertyChanged(visionName: Name, propertyName: Name, item: Meta?)
}


public fun VisionClient.notifyPropertyChanged(visionName: Name, propertyName: String, item: Meta?) {
    notifyPropertyChanged(visionName, propertyName.parseAsName(true), item)
}

public fun VisionClient.notifyPropertyChanged(visionName: Name, propertyName: String, item: Number) {
    notifyPropertyChanged(visionName, propertyName.parseAsName(true), Meta(item))
}

public fun VisionClient.notifyPropertyChanged(visionName: Name, propertyName: String, item: String) {
    notifyPropertyChanged(visionName, propertyName.parseAsName(true), Meta(item))
}

public fun VisionClient.notifyPropertyChanged(visionName: Name, propertyName: String, item: Boolean) {
    notifyPropertyChanged(visionName, propertyName.parseAsName(true), Meta(item))
}

public fun VisionClient.sendEvent(targetName: Name, payload: MetaRepr): Unit {
    context.launch {
        sendEvent(targetName, VisionMetaEvent(payload.toMeta()))
    }
}