# DataForge Plugins for Visualisation

This repository contains [DataForge](http://npm.mipt.ru/dataforge/) 
(also [here](https://github.com/mipt-npm/dataforge-core)) components useful for visualization in
various scientific applications. Currently, the main application is 3D visualization for accelerator 
experiments.

The project is developed as a Kotlin multiplatform application, currently 
targeting browser JavaScript and JVM.

## Modules contained in this repository:

### dataforge-vis-common 

Common visualisation objects such as VisualObject and VisualGroup.

### dataforge-vis-fx

JavaFX utilities for meta manipulations.

### dataforge-vis-jsroot

Some JSROOT bindings. 

Note: Currently, this part is experimental and put here for completeness. This module will not build.  

### dataforge-vis-spatial

Includes common description and serializers for 3D visualisation, JavaFX and Three.js implementations.

### dataforge-vis-spatial-gdml

GDML bindings for 3D visualisation (to be moved to gdml project)

### spatial-js-demo

Contains a simple demonstration. To see the demo: run 
`spatial-js-demo/distribution/installJsDist` Gradle task, then open
`build/distribuions/spatial-js-demo-0.0.0-dev/index.html`.  

