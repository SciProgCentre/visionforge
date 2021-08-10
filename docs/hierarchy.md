# Hierarchy

![](../docs/images/hierarchy.png)

### Vision

* function `getProperty(name: Name, inherit: Boolean, includeStyles: Boolean, includeDefaults: Boolean)`. 

It gets properties of element with `name` identification.
`inherit` &mdash; toggles parent node property lookup. Null means inference from descriptor. Default is false.
`includeStyles` &mdash; toggles inclusion of. Null means inference from descriptor. Default is true. 
`includeDefaults` &mdash; default is false.

* function `setProperty(name: Name, item: MetaItem?, notify: Boolean = true)`

Sets the `item` property to the element with the `name` identification. `notify` is a value which toggles the necessity of the change notification. Default is true.

### About properties
**Properties have to be set in particular order:**

* styles
* prototypes
* parent
* parent's styles
* defaults


## Inheritance

Inheritance is a very useful ability of `children` elements to get the same property in default as his parent does have (to 'inherit' it). 


