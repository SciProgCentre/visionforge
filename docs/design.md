## Library design
The central point of the library design is the `Vision` interface. The `Vision` stores an optional reference to its parent and is able to store a number of mutable or read-only properties. Each property is represented by its `Name`, and a `MetaItem` value-tree, both following DataForge library specification (discussed in the [Appendix](appendix.md)). The `Vision` objects are organized in a tree using `VisionGroup` as nodes. `VisionGroup` additionally to all `Vision` properties holds a `children` container that holds named references to its direct children `Vision`s. Thus, `Vision`s form a doubly linked tree (a parent stores references to all its children and children store a reference to the parent).

An important concept using in the VisionForge is the property layering mechanism. It means that if the property with a given name is not found in the `Vision` it is requested from, it could be requested from the parent `Vision`, form the style declaration, the prototype for the vision or any other place defined by the component author. For example, let's take a `color` attribute used in 3D visualization. When one draws a group of objects, he usually wants to make the color of all objects in the group to be defined by a single handle in the group common ancestor. So when the parent color changes, all children color must follow suite, but we also want to change children color individually without changing the parent. In this case two property layers are defined:

* own properties layer
* parent properties layer

When the user or a part of inner program mechanics requests a property, it searched in `Vision` own properties. If it is defined there, it is returned, if not, the search continues to the parent properties and up the parentage chain. So if the parent color is defined and children colors are blank, then the parent color will be used. If one of children redefines its own color, then this color is used for this specific color, and the parent color used for all other children. The change of parent property automatically propagated to all children.

### Styling

The actual layering scheme is more complicated. All objects support styling. The styles are named `Meta` nodes that could be used to define groups of properties together and use in different `Vision`s. The styles are defined as named `Meta` nodes in a `VisionGroup` `@stylesheet` property (DataForge states that names starting with `@` are reserved for system properties, not visible by default to the user). The style or a list of styles could be applied to a `Vision` by setting the reserved `@style` property to the name of the style being used or by using `Vision::style` extension property. The style name is then resolved to the nearest ancestor that defines it. The key difference from the CSS styling in HTML is that the stylesheets could be added to any node, not only to the hierarchy root. It allows to decouple the whole subtrees together with styles from the main `Vision` graph, or create stand-alone branches. One must note, that the same style could be defined on different levels of the tree, only the nearest ancestor is resolved, meaning that one can override styles for a sub-tree.

### Intermediate representation

An important thing about VisionForge is that it does not strictly bound to a single format representation. 

### Kotlin DSL for creating vision-graphs