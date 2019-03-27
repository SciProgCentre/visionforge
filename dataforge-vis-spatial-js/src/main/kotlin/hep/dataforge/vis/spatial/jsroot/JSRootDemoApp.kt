package hep.dataforge.vis.spatial.jsroot

import hep.dataforge.context.Global
import hep.dataforge.vis.ApplicationBase
import hep.dataforge.vis.DisplayGroup
import hep.dataforge.vis.require
import hep.dataforge.vis.spatial.ThreeOutput
import hep.dataforge.vis.spatial.render
import kotlin.browser.document


class JSRootDemoApp : ApplicationBase() {

    override val stateKeys: List<String> = emptyList()

    override fun start(state: Map<String, Any>) {
        require("JSRootGeoBase.js")


        //TODO remove after DI fix
//        Global.plugins.load(ThreePlugin())
//        Global.plugins.load(JSRootPlugin())

        Global.plugins.load(JSRootPlugin)

        val renderer = ThreeOutput(Global)
        renderer.start(document.getElementById("canvas")!!)
        println("started")

        lateinit var group: DisplayGroup

        renderer.render {
            jsRoot ("./geofile_full.json")
        }

    }

    override fun dispose() = emptyMap<String, Any>()//mapOf("lines" to presenter.dispose())
}