package space.kscience.plotly

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * Create a standalone html with the plot
 * @param path the reference to html file. If null, create a temporary file
 * @param config represents plotly frame configuration
 */
@UnstablePlotlyAPI
public fun Plot.makeFile(
    path: Path,
    config: PlotlyConfig = PlotlyConfig(),
) {
    FileSystem.SYSTEM.write(path, true) {
        writeUtf8(toHTMLPage(cdnPlotlyHeader, config = config))
    }
}

@UnstablePlotlyAPI
public fun Plot.makeFile(
    path: String,
    config: PlotlyConfig = PlotlyConfig(),
) {
    makeFile(path.toPath(), config)
}