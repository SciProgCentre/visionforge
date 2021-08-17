# Hierarchy

![](../docs/images/hierarchy.png) 
**the image will be changed**

### Vision

* function `getProperty(name: Name, inherit: Boolean, includeStyles: Boolean, includeDefaults: Boolean)`. 

It gets properties of element with `name` identification.
`inherit` &mdash; toggles parent node property lookup. Null means inference from descriptor. Default is false.
`includeStyles` &mdash; toggles inclusion of. Null means inference from descriptor. Default is true. 
`includeDefaults` &mdash; default is false.

* function `setProperty(name: Name, item: MetaItem?, notify: Boolean = true)`

Sets the `item` property to the element with the `name` identification. `notify` is a value which toggles the necessity of the change notification. Default is true.
