package hep.dataforge.vision

import hep.dataforge.meta.Config
import hep.dataforge.meta.Meta
import hep.dataforge.meta.get
import hep.dataforge.meta.set
import hep.dataforge.names.NameToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex


//public class VisionChange(public val properties: Meta, public val childrenChange: Map<NameToken, VisionChange>)

public class VisionChangeCollector(
    public val manager: VisionManager,
    public val scope: CoroutineScope,
    public val vision: Vision,
    public val lock: Mutex = Mutex()
) {
    private val collector: Config = Config()
    private val childrenCollectors = HashMap<NameToken, VisionChangeCollector>()

    init {
        vision.onPropertyChange(this) { propertyName ->
            collector[propertyName] = vision.properties?.get(propertyName)
        }
        if (vision is VisionGroup) {
            vision.children.forEach { (token, child) ->
                childrenCollectors[token] = VisionChangeCollector(manager, scope, child, lock)
            }
        }
        if (vision is MutableVisionGroup) {
            TODO("Tread vision structure change")
//            vision.onChildrenChange(this) { childName, child ->
//                if(child == null){
//                    childrenCollectors[childName] = null
//                } else {
//                    childrenCollectors[childName] = manager.encodeToMeta(child)
//                }
//            }
        }
    }
}
//
//fun collectUpdates(manager: VisionManager, scope: CoroutineScope, vision: Vision): Flow<Meta> {
//
//
//    vision.
//}
