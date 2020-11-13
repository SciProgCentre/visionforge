package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.meta.descriptors.NodeDescriptor
import hep.dataforge.names.Name
import hep.dataforge.names.NameToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

/**
 * An empty vision existing only for Vision tree change representation. [NullVision] should not be used outside update logic.
 */
@Serializable
@SerialName("vision.null")
public object NullVision : Vision {

    @Suppress("SetterBackingFieldAssignment")
    override var parent: VisionGroup? = null
        set(value) {
            //do nothing
        }

    override val properties: Config? = null

    override fun getAllProperties(): Laminate = Laminate(Meta.EMPTY)

    override fun getProperty(name: Name, inherit: Boolean): MetaItem<*>? = null

    override fun propertyChanged(name: Name) {}

    override fun onPropertyChange(owner: Any?, action: (Name) -> Unit) {}

    override fun removeChangeListener(owner: Any?) {}

    override fun update(change: Vision) {
        error("Null vision should be removed, not updated")
    }

    override val config: Config get() = Config()
    override val descriptor: NodeDescriptor? get() = null
}

private fun Vision.collectChange(scope: CoroutineScope, collector: Vision): Job = scope.launch {
    //Store job to be able to cancel collection jobs
    //TODO add lock for concurrent modification protection?
    val jobStore = HashMap<NameToken, Job>()

    if (this is VisionGroup) {
        check(collector is MutableVisionGroup) { "Collector for a group should be a group" }
        //Subscribe for children changes
        children.forEach { (token, child) ->
            val childCollector: Vision = if (child is VisionGroup) {
                VisionGroupBase()
            } else {
                VisionBase()
            }
            val job = child.collectChange(this, childCollector)
            jobStore[token] = job
            //TODO add lazy child addition
            collector[token] = childCollector
        }

        //Subscribe for structure change
        if (this is MutableVisionGroup) {
            onChildrenChange(this) { token, child ->
                //Cancel collector job to avoid leaking
                jobStore[token]?.cancel()
                if (child != null) {
                    //Collect to existing Vision
                    val job = child.collectChange(this, child)
                    jobStore[token] = job
                    collector[token] = child
                } else{
                    collector[token] = NullVision
                }
            }
        }
    }

    //Collect properties change
    collector.onPropertyChange(collector) { propertyName ->
        collector.config[propertyName] = properties?.get(propertyName)
    }
}

public fun Vision.flowChanges(scope: CoroutineScope, collectionDuration: Duration): Flow<Vision> = flow {
    //emit initial visual tree
    emit(this@flowChanges)
    while (true) {
        val collector: Vision = if (this is VisionGroup) {
            VisionGroupBase()
        } else {
            VisionBase()
        }
        val collectorJob = collectChange(scope, collector)
        kotlinx.coroutines.delay(collectionDuration)
        emit(collector)
        collectorJob.cancel()
    }
}