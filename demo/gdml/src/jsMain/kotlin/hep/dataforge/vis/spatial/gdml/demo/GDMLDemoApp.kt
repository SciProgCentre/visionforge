package hep.dataforge.vis.spatial.gdml.demo

import hep.dataforge.context.Global
import hep.dataforge.js.Application
import hep.dataforge.js.startApplication
import hep.dataforge.vis.spatial.Material3D.Companion.MATERIAL_OPACITY_KEY
import hep.dataforge.vis.spatial.gdml.GDMLTransformer
import hep.dataforge.vis.spatial.gdml.LUnit
import hep.dataforge.vis.spatial.gdml.toVisual
import react.child
import react.dom.render
import kotlin.browser.document


val gdmlConfiguration: GDMLTransformer.() -> Unit = {
    lUnit = LUnit.CM
    volumeAction = { volume ->
        when {
            volume.name.startsWith("ecal01lay") -> GDMLTransformer.Action.REJECT
            volume.name.startsWith("UPBL") -> GDMLTransformer.Action.REJECT
            volume.name.startsWith("USCL") -> GDMLTransformer.Action.REJECT
            volume.name.startsWith("VPBL") -> GDMLTransformer.Action.REJECT
            volume.name.startsWith("VSCL") -> GDMLTransformer.Action.REJECT
            else -> GDMLTransformer.Action.CACHE
        }
    }

    solidConfiguration = { parent, solid ->
        if (
            solid.name.startsWith("Yoke")
            || solid.name.startsWith("Pole")
            || parent.physVolumes.isNotEmpty()
        ) {
            useStyle("opaque") {
                MATERIAL_OPACITY_KEY put 0.3
            }
        }
    }
}

private class GDMLDemoApp : Application {

    override fun start(state: Map<String, Any>) {

        val context = Global.context("demo") {}
        val element = document.getElementById("app") ?: error("Element with id 'app' not found on page")

        render(element) {
            child(GDMLApp) {
                attrs {
                    this.context = context
                    this.rootObject = cubes().toVisual(gdmlConfiguration)
                }
            }
        }
//        (document.getElementById("file_load_button") as? HTMLInputElement)?.apply {
//            addEventListener("change", {
//                (it.target as HTMLInputElement).files?.asList()?.first()?.let { file ->
//                    FileReader().apply {
//                        onload = {
//                            val string = result as String
//                            action(file.name, string)
//                        }
//                        readAsText(file)
//                    }
//                }
//            }, false)
//        }

    }
}

fun main() {
    startApplication(::GDMLDemoApp)
}