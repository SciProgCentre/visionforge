package hep.dataforge.vision.visitor

import hep.dataforge.names.Name
import hep.dataforge.names.plus
import hep.dataforge.vision.Vision
import hep.dataforge.vision.VisionGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

interface VisionVisitor {
    /**
     * Visit a vision possibly mutating in in the process. Should not rearrange children.
     * @param name full name of a [Vision] being visited
     * @param vision the visited [Vision]
     */
    suspend fun visit(name: Name, vision: Vision)

    /**
     * Rearrange children of given group
     */
    suspend fun visitChildren(name: Name, group: VisionGroup) {
        //Do nothing by default
    }

    fun skip(name: Name, vision: Vision): Boolean = false

    companion object{
        private fun CoroutineScope.visitTreeAsync(
            visionVisitor: VisionVisitor,
            name: Name,
            vision: Vision
        ): Job = launch {
            if (visionVisitor.skip(name, vision)) return@launch
            visionVisitor.visit(name, vision)

            if (vision is VisionGroup) {
                visionVisitor.visitChildren(name, vision)

                for ((token, child) in vision.children) {
                    visitTreeAsync(visionVisitor, name + token, child)
                }
            }
        }

        /**
         * Recursively visit this [Vision] and all children
         */
        fun visitTree(visionVisitor: VisionVisitor, scope: CoroutineScope, root: Vision): Job =
            scope.visitTreeAsync(visionVisitor, Name.EMPTY, root)


    }
}

