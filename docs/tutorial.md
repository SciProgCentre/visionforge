#Tutorial

###The main goal of this tutorial is to show all capabilities of ... (this part will be supplemented)

The simple visualization can be made with function `main`. (this part will be supplemented as well)
```kotlin
import kotlinx.html.div
import space.kscience.dataforge.context.Context
import space.kscience.visionforge.html.ResourceLocation
import space.kscience.visionforge.solid.*
import java.nio.file.Paths

fun main(){
    val context = Context{
        plugin(Solids)
    }

    context.makeVisionFile (
        Paths.get("customFile.html"),
        resourceLocation = ResourceLocation.EMBED
    ){
        div {
            vision {
                solid {
                }
            }
        }
    }
}
```
##Solids properties
**We will analyze which basic properties solids have using `box` solid.**

Basic properties:
1. `opacity` - It is set in `float`. It takes on values from 0 to 1, which represent percents of solid opacity. It's initial value is 1.
2. `color` - It can be specified as `Int`, `String`, or as three `Ubytes`, which represent color in `rgb`. Elementally, the solid will have `green` color.
3. `rotation` - it's the point, around which the solid will be rotated. Initially, the value is `Point3D(0, 0, 0)`
4. position, which is given by values `x`, `y`, `z`. Initial values are `x = 0`, `y = 0`, `z = 0`

Let's see how properties are set in solids.
The `small box` will have elemental values of properties. If you will not set properties, it will have the same `position`, `color`, `rotation`, and `opacity` values.

***You can see that `box` take four values. Later, we will discuss what they are doing in more detail. Now, it does not really matter.***
```kotlin
box(10, 10, 10, name = "small box"){
   x = 0
   y = 0
   z = 0
   opacity = 1 //100% opacity
   color("red") //as string
   rotation = Point3D(0, 0, 0)
}
```
![](../docs/images/small-box.png)

The `big box` will have properties with custom values. 
```kotlin
box(40, 40, 40, name = "big box"){
   x = 20
   y = 10
   z = 60
   opacity = 0.5 //50% opacity
   color(0u, 179u, 179u) //color in rgb
   rotation = Point3D(60, 80, 0)
}
```
![](../docs/images/big-rotated-box.png)

If we compare these boxes, we will see all differences. 

Here is the function `main` with both boxes.
```kotlin
fun main(){
    val context = Context{
        plugin(Solids)
    }

    context.makeVisionFile (
        Paths.get("customFile.html"),
        resourceLocation = ResourceLocation.EMBED
    ){
        div {
            vision {
                solid {
                   box(10, 10, 10, name = "small box"){
                      x = 0
                      y = 0
                      z = 0
                      opacity = 1 //100% opacity
                      color("red") //as string
                      rotation = Point3D(0, 0, 0)
                   }
                   box(40, 40, 40, name = "big box"){
                      x = 20
                      y = 10
                      z = 60
                      opacity = 0.5 //50% opacity
                      color(0u, 179u, 179u) //rgb
                      rotation = Point3D(60, 80, 0)
                   }
                }
            }
        }
    }
}
```
![](../docs/images/two-boxes-1.png)
![](../docs/images/two-boxes-2.png)

###Basic Solids
Now, let's see which solids can be visualized:
1) PolyLine
2) Box
   ```kotlin
   box(50, 50, 50, name = "box") {
        x = 0
        y = 0
        z = 0
        color("pink")
   }
   ```
   ![](../docs/images/box.png)
   ```kotlin
   box(10, 25, 10, name = "high_box") {
        x = 0
        y = 0
        z = 0
        color("black")
   }
   ```
   ![](../docs/images/high-box.png)
   
   ```kotlin
   box(65, 40, 40, name = "wide_box") {
        x = 0
        y = 0
        z = 0
        color("black")
   }
   ```
   ![](../docs/images/wide-box.png)
   
3) Sphere
   ```kotlin
   sphere(50, name = "sphere") {
        x = 0
        y = 0
        z = 0
        color("blue")
   }
   ```
   ![](../docs/images/sphere.png)
4) Hexagon
   ```kotlin
   hexagon(
        Point3D(25, 30, 25),
        Point3D(35, 30, 25),
        Point3D(35, 30, 15),
        Point3D(25, 30, 15),
        Point3D(30, 18, 20),
        Point3D(40, 18, 20),
        Point3D(40, 18, 10),
        Point3D(30, 18, 10),
        name = "classic_hexagon"){
        color("green")
   }
   ```
   ![](../docs/images/classic-hexagon.png)
   ```kotlin
   hexagon(
        Point3D(5, 30, 5),
        Point3D(24, 30, 8),
        Point3D(20, 30, -10),
        Point3D(5, 30, -7),
        Point3D(8, 16, 0),
        Point3D(12, 16, 0),
        Point3D(10, 16, -5),
        Point3D(6.5, 12, -3),
        name = "custom_hexagon"){
        color("brown")
   }
   ```
   ![](../docs/images/custom-hexagon.png)
5) Cone
   ```kotlin
   cone(60, 80, name = "cone") {
         x = 0
         y = 0
         z = 0
         color("beige")
   }
   ```
   ![](../docs/images/cone-1.png)
   ![](../docs/images/cone-2.png)
6) Cone Surface
   ```kotlin
   coneSurface(60, 50, 30, 10, 100, name = "cone_surface") {
        x = 0
        y = 0
        z = 0
        color("red")
        rotation = Point3D(2, 50, -9)
   }
   ```
   ![](../docs/images/cone-surface-1.png)
   ![](../docs/images/cone-surface-2.png)
7) Extruded

