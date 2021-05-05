package space.kscience.visionforge.gdml.jupyter

import kotlinx.css.ListStyleType
import kotlinx.css.listStyleType
import space.kscience.dataforge.misc.DFExperimental
import space.kscience.visionforge.bootstrap.useBootstrap
import space.kscience.visionforge.runVisionClient
import styled.injectGlobal

@DFExperimental
fun main(): Unit = runVisionClient {
    useBootstrap()
    injectGlobal {
        rule("ul.nav") {
            listStyleType = ListStyleType.none
        }

        rule(".treeStyles-tree") {
            listStyleType = ListStyleType.none
        }

        rule("ol.breadcrumb") {
            listStyleType = ListStyleType.none
        }
    }
    plugin(ThreeWithControls)
}