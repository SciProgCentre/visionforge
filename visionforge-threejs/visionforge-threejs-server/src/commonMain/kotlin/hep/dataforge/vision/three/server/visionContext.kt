package hep.dataforge.vision.three.server

import hep.dataforge.context.Context
import hep.dataforge.vision.VisionManager

public expect val visionContext: Context

public val visionManager: VisionManager get() = visionContext.plugins.fetch(VisionManager)