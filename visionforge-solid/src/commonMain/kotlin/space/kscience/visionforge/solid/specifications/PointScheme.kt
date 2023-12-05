package space.kscience.visionforge.solid.specifications

import space.kscience.dataforge.meta.Scheme
import space.kscience.dataforge.meta.SchemeSpec
import space.kscience.dataforge.meta.double

public class PointScheme: Scheme(){
    public var x: Double? by double()
    public var y: Double? by double()
    public var z: Double? by double()

    public companion object: SchemeSpec<PointScheme>(::PointScheme)
}

public operator fun PointScheme.invoke(x: Number?, y: Number?, z: Number?){
    this.x = x?.toDouble()
    this.y = y?.toDouble()
    this.z = z?.toDouble()
}