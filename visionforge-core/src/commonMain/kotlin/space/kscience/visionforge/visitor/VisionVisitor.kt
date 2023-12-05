package space.kscience.visionforge.visitor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.plus
import space.kscience.visionforge.Vision
import space.kscience.visionforge.children
import space.kscience.visionforge.forEach

public interface VisionVisitor {
    /**
     * Visit a vision possibly mutating in in the process. Should not rearrange children.
     * @param name full name of a [Vision] being visited
     * @param vision the visited [Vision]
     */
    public suspend fun visit(name: Name, vision: Vision)

    /**
     * Rearrange children of given group
     */
    public suspend fun visitChildren(name: Name, group: Vision) {
        //Do nothing by default
    }

    public fun skip(name: Name, vision: Vision): Boolean = false

    public companion object {
        private fun CoroutineScope.visitTreeAsync(
            visionVisitor: VisionVisitor,
            name: Name,
            vision: Vision,
        ): Job = launch {
            if (visionVisitor.skip(name, vision)) return@launch
            visionVisitor.visit(name, vision)


            visionVisitor.visitChildren(name, vision)

            vision.children?.forEach { token, child ->
                visitTreeAsync(visionVisitor, name + token, child)
            }
        }


        /**
         * Recursively visit this [Vision] and all children
         */
        public fun visitTree(visionVisitor: VisionVisitor, scope: CoroutineScope, root: Vision): Job =
            scope.visitTreeAsync(visionVisitor, Name.EMPTY, root)


    }
}

