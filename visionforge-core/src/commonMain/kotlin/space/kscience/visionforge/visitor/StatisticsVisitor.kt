package space.kscience.visionforge.visitor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.length
import space.kscience.visionforge.Vision
import kotlin.reflect.KClass


@OptIn(ExperimentalCoroutinesApi::class)
public suspend fun <T> Vision.flowStatistics(statistics: (Name, Vision) -> T): Flow<T> = callbackFlow<T> {
    val visitor = object : VisionVisitor {
        override suspend fun visit(name: Name, vision: Vision){
            send(statistics(name, vision))
        }
    }
    val job: Job = VisionVisitor.visitTree(visitor, this, this@flowStatistics)
    job.invokeOnCompletion {
        channel.close()
    }
    awaitClose {
        job.cancel()
    }
}

public data class DefaultVisionStatistics(val name: Name, val type: KClass<out Vision>) {
    val depth: Int get() = name.length
}

public suspend fun Vision.flowStatistics(): Flow<DefaultVisionStatistics> = flowStatistics { name, vision ->
    DefaultVisionStatistics(name, vision::class)
}