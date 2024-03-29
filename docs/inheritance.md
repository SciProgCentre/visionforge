## Inheritance

Inheritance is an ability of an element to transfer a pack of properties to its `children` elements 'wrapped inside'.

Properties, which can be inherited by objects, are `styles`, `prototypes` (if the object is a reference), `inherit` (it stands for the presence of `parent` objects), and `defaults`. 

All values of `styles` property are contained in class `StyleSheet`, where they all are defined at `Group`s level. The `prototypes` property tree is defined in `SolidGroup` class via `PrototypeHolder` interface, and
`SolidReference` class helps to reuse a template object. 

The order of inheritance of properties is set in function `getProperty` in `VisionBase` class.
The order is this:
* own styles
* prototypes
* parent
* parent's styles
* defaults


Let's take a closer look using a [Muon Monitor Visualization](../demo/muon-monitor/README.md).
Running the demo, we will see this:

![](../docs/images/inheritance-1.png)

You can see a tree of elements on the left; 'World' is a `root`, 'bottom', 'middle', and 'top' are 'World's `children` and so on.

![](../docs/images/inheritance-tree.png)

On the right, there is a list with changeable properties.

![](../docs/images/inheritance-properties.png)

Properties, which can or cannot be inherited, are these:
* `visible` &ndash; toggles the visibility of an element. To be exact, the invisibility of an element is inheritable.
  If a `parent` element is invisible, other elements are invisible as well, and they cannot be changed to visible mode.
  ![](../docs/images/inheritance-2-1-1.png)
  ![](../docs/images/inheritance-2-1-2.png)

* `material` &ndash; a group of properties, which can be inherited and which can be changed in `children` elements.
  * `color` &ndash; color of an element.
  * `opacity` &ndash; a number from 0 to 1 which represents percents of opacity (0 for 0%, 1 for 100%).
  * `wireframe` &ndash; toggles the wireframe mode.

  Let's see how elements of the `material` group inherit changing `color` property; ***other properties of this group inherit in the same way.***

  Let's change color of 'World' element:
  ![](../docs/images/inheritance-2-2-1.png)
  It is a `parent`, so 'bottom', 'middle', and 'top' elements inherit this color.

  Now, let's change 'top's color:
  ![](../docs/images/inheritance-2-2-2.png)
  It changes only, 'bottom' and 'middle' stays the same.

  'top' is a `parent` element as well: it has `children` &ndash; 'SC72', 'SC73', ... ,'SC80'.
  Let's change the color of 'SC76':
  ![](../docs/images/inheritance-2-2-3.png)
  Again, only 'SC76' has changed among other 'siblings'.

  However, 'SC76' is a `parent` too. Let's change one of its `children` color (here we change 'SC76_5's color'):
  ![](../docs/images/inheritance-2-2-4.png)

  As we can see, `color` is inheritable property, which can be changed in a custom way.

  If after all those changes we set at the 'World' element grey color, changes won't disappear:
  ![](../docs/images/inheritance-2-2-5.png)

* `rotation` &ndash; rotation of an element. Here, it is set by `x` value. It is inheritable and unable to be changed in `children` elements.
* `position` &ndash; position of an element, cannot be inherited.
