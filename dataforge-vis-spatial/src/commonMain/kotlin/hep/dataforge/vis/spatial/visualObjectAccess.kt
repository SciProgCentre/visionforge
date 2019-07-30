package hep.dataforge.vis.spatial

var VisualObject3D.x: Number
    get() = position.x
    set(value) {position.x = value.toFloat()}

var VisualObject3D.y: Number
    get() = position.y
    set(value) {position.y = value.toFloat()}

var VisualObject3D.z: Number
    get() = position.z
    set(value) {position.z = value.toFloat()}

var VisualObject3D.rotationX: Number
    get() = rotation.x
    set(value) {rotation.x = value.toFloat()}

var VisualObject3D.rotationY: Number
    get() = rotation.y
    set(value) {rotation.y = value.toFloat()}

var VisualObject3D.rotationZ: Number
    get() = rotation.z
    set(value) {rotation.z = value.toFloat()}

var VisualObject3D.scaleX: Number
    get() = scale.x
    set(value) {scale.x = value.toFloat()}

var VisualObject3D.scaleY: Number
    get() = scale.y
    set(value) {scale.y = value.toFloat()}

var VisualObject3D.scaleZ: Number
    get() = scale.z
    set(value) {scale.z = value.toFloat()}