package three.meshline

import three.core.BufferGeometry
import three.math.Vector3

public fun MeshLine(geometry: BufferGeometry): MeshLine = MeshLine().apply { setGeometry(geometry) }

public fun MeshLine(points: Array<Vector3>): MeshLine = MeshLine().apply { setPoints(points) }