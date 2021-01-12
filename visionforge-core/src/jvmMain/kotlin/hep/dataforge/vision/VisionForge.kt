package hep.dataforge.vision

import hep.dataforge.context.Context
import hep.dataforge.context.Global

public actual val VisionForge: Context = Global.context("VisionForge").apply{
    plugins.fetch(VisionManager)
}