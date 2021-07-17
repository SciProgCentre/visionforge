## Solids

The primary initial goal of creating the library was to provide a flexible API and rendering engine to draw solid geometric forms for so-called event display task in particle physics. Event display is used to provide visualization for particle hit events in both accelerator and non-accelerator experiments (see [add links here] for examples). The event display system must have a way to render basic 3D shapes like boxes, tubes and spheres and particle tracks. An additional requirement is to be able to change coloring and transparency of different elements as well as custom attributes specific to the problem.

The `Solid` class inherits the `Vision` and has three additional nullable real vector properties: `position`, `rotation` and `scale`. Those properties are placed on the top level of the class instead of `properties` tree for performance optimization purposes. The `position` and `rotation` properties reflect the position offset in absolute units and rotation in radians relative to the parent and have defaults of `0.0`. Relativity means that if the parent `Vision` (`SolidGroup`) has `position = (1.0, 0.0, 1.0)` and a child has `position = (0.0, 2.0, 0.0)`, then the real offset of rendered object will be `(1.0, 2.0, 1.0)`. The same goes for the rotation. By default, the rotation uses intrinsic Cordan (Tait–Bryan) angles with `XYZ` order, but order could be changed with an inherited property `rotation.order`. The `scale` property is not inherited and represents local `Solid` scaling factor with default value of `1.0`.

The `SolidGroup` represents the specific kind of `VisionGroup` that has properties of a `Solid` and has one additional special property: the prototype container.

### Solid prototypes

One of the important requirements for event display system is the ability to render a huge number of primitives (tens and hundreds of thousands). This is especially important for accelerator experiments with fine-grained calorimeter detectors ([ref here]). The most memory and CPU-consuming task to render such structures is computing the geometry o equal elements (like calorimeter cells). In order to optimize rendering and model representation for such cases, we introduced so-called prototypes. The idea is that multiple visions could have the same prototype and share the definition and actual rendered geometry. The prototypes are stored in a special container property called `prototypes` in a `SolidGroup` object. In order to access those prototypes there is a special `Solid` object called `SolidReference`. It inherits `Solid` (has properties container, position, rotation and scale), but does not define any geometry. Instead, it has a single `refName` property which stores a fully qualified DataForge `Name` (see [Appendix](appendix.md)) that designates the name of the prototype to be used. During rendering phase, the system searches for appropriate prototype in the nearest parent. If the prototype is not found, the search continues in the next parent in the chain. Like property resolution, the prototype resolution is fine-grained and local, meaning that a part of `Vision` graph could be self-contained together with prototypes.

An important point is that prototypes are resolved in a parent vision container, and the parent vision container for prototypes is the vision group. It means that prototype itself could reference a prototype, or even in corner cases reference itself (such cases should be checked for infinite loops). The prototype relation example is shown in [prototypes.uml](uml/prototypes.puml). Here `Prototype Ref` vision references the `Prototype` vision by first seeking up to `VirionGroup`, the resolving the prototype with appropriate name. In theory, vision groups inside prototypes groups could contain the prototypes of their own, but such constructs should be used with care.

### Solid definitions

Solid models are defined as kotlin classes that also serve as builders for DSL and serialization models. For example, here is the definition for a `Box` class:

```kotlin
@Serializable
@SerialName("solid.box")
class Box(
    val xSize: Float,
    val ySize: Float,
    val zSize: Float
) : SolidBase(), GeometrySolid 
```

In general, one does not define how specific solid is rendered, it is done in the rendering back-end. It means that in general, there should be a specific renderer defined for each type of solids defined in the model. Some solids have additional mechanism to avoid providing renderers for each primitive. Classes that inherit `GeometrySolid` interface provide a way to define geometry via polygons like this:

```kotlin
    override fun <T : Any> toGeometry(geometryBuilder: GeometryBuilder<T>) {
        val dx = xSize / 2
        val dy = ySize / 2
        val dz = zSize / 2
        val node1 = Point3D(-dx, -dy, -dz)
        val node2 = Point3D(dx, -dy, -dz)
        val node3 = Point3D(dx, dy, -dz)
        val node4 = Point3D(-dx, dy, -dz)
        val node5 = Point3D(-dx, -dy, dz)
        val node6 = Point3D(dx, -dy, dz)
        val node7 = Point3D(dx, dy, dz)
        val node8 = Point3D(-dx, dy, dz)
        geometryBuilder.face4(node1, node4, node3, node2)
        geometryBuilder.face4(node1, node2, node6, node5)
        geometryBuilder.face4(node2, node3, node7, node6)
        geometryBuilder.face4(node4, node8, node7, node3)
        geometryBuilder.face4(node1, node5, node8, node4)
        geometryBuilder.face4(node8, node5, node6, node7)
    }
```

The `GeometryBuilder` instance is provided by the specific rendering engine, so the polygons are generated for target engine without intermediate steps. If the renderer provides its own implementation for a solid renderer, it takes precedence over generic `GeometrySolid` definition.

An important basic solid is the `Composite`. It is a primitive, that allows combine two (maybe more in future) primitives via [composite solid geometry or CSG](https://en.wikipedia.org/wiki/Constructive_solid_geometry) transformations (union, difference and subtraction). At this moment, the actual transformation is done on the renderer side.

### GDML bindings

VisionForge supports extensible geometry primitive list, but we've started with primitives needed to render BM@N ([ref]) GDML ([ref]) geometry. GDML files are read with [gdml.kt](https://github.com/mipt-npm/gdml.kt) package designed specifically for this purpose. The package uses [https://github.com/pdvrieze/xmlutil](https://github.com/pdvrieze/xmlutil) serialization plugin to parse GDML structure into the convenient object structure and has language definitions for all GDML primitives. The `visionforge-gdml` module contains a converter that transforms Gdml.kt structures into `Solid`.

The GDML file definition contains several sections:

* **defines** - Dimensions and numbers for geometries.
* **materials** - Material definitions.
* **solids** - Primitive definitions and their composition.
* **structure** - Combinations of solids and groups used in geometry.
* **setup** - Metadata and geometry entry point.

GDML materials are currently used only for Solid coloring. The data from material is stored in the `Vision` properties. Solids section and structures are transformed into top level group prototypes, which allows not only to create a compact representation, but also optimize rendering. The gdml structure is not always flexible. For example, it is not possible to add a solid directly into a group, but instead one needs to add a group with a single element. Problems like this could be automatically optimized by the convertor.

Not all GDML solids are currently supported by the converter, they could be added later on-demand. Current support covers all solids used in BM@N geometry.

### ThreeJs renderer

VisionForge is not tied to any single renderer. Right now the primary target is the Browser rendering with [Three.js library](https://threejs.org/). The Three.js supports different renderers including WebGL with hardware support and virtual reality.

The bindings for three-js was implemented in kotlin-js based on a [work by Lars Ivar Hatledal](https://github.com/markaren/three-kt-wrapper). The wrapper allows seamless integration with a lot of different library APIs including custom cameras and CSG.