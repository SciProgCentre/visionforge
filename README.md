[![JetBrains Research](https://jb.gg/badges/research.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)

# DataForge Visualization Platform

## Table of Contents

* [Introduction](#introduction)
* [Features](#features)
* [About DataForge](#about-dataforge)
* [Modules contained in this repository](#modules-contained-in-this-repository)
  * [dataforge-vis-common](#dataforge-vis-common)
  * [dataforge-vis-spatial](#dataforge-vis-spatial)
  * [dataforge-vis-spatial-gdml](#dataforge-vis-spatial-gdml)
  * [dataforge-vis-jsroot](#dataforge-vis-jsroot)
* [Demonstrations](#demonstrations)
  * [Simple Example - Spatial Showcase](#simple-example-spatial-showcase)
  * [Full-Stack Application Example - Muon Monitor](#full-stack-application-example-muon-monitor-visualization)
  * [GDML Example](#gdml-example)


## Introduction

This repository contains a [DataForge](#about-dataforge)\-based framework 
used for visualization in various scientific applications. 

The main framework's use case for now is 3D visualization for particle physics experiments. 
Other applications including 2D plots are planned for the future.

The project is being developed as a [Kotlin multiplatform](https://kotlinlang.org/docs/reference/multiplatform.html) 
application, currently targeting browser JavaScript and JVM.


## Features

The main framework's features for now include:
- 3D visualization of complex experimental set-ups
- Event display such as particle tracks, etc.
- Scales up to few hundred thousands of elements
- Camera move, rotate, zoom-in and zoom-out
- Scene graph as an object tree with property editor
- Settings export and import
- Multiple platform support


## About DataForge

DataForge is a software framework for automated scientific data processing. DataForge Visualization
Platform uses some of the concepts and modules of DataForge, including: `Meta`, `Configuration`, `Context`,
`Provider`, and some others.

To learn more about DataForge, please consult the following URLs:
 * [Kotlin multiplatform implementation of DataForge](https://github.com/mipt-npm/dataforge-core)  
 * [DataForge documentation](http://npm.mipt.ru/dataforge/) 
 * [Original implementation of DataForge](https://bitbucket.org/Altavir/dataforge/src/default/)


## Modules contained in this repository

### dataforge-vis-common 

Contains a general hierarchy of classes and interfaces useful for visualization. 
This module is not specific to 3D-visualization.

The `dataforge-vis-common` module also includes configuration editors for JS (in `jsMain`) and JVM (in `jvmMain`).

##### Class diagram: 

![](doc/resources/class-diag-common.png)


### dataforge-vis-spatial

Includes common classes and serializers for 3D visualization, Three.js and JavaFX implementations.

##### Class diagram:

![](doc/resources/class-diag-3d.png)

##### Prototypes

One of the important features of the framework is support for 3D object prototypes (sometimes
also referred to as templates). The idea is that prototype geometry can be rendered once and reused 
for multiple objects. This helps to significantly decrease memory usage.

The `prototypes` property tree is defined in `VisualGroup3D` class, and `Proxy` class helps to reuse a template object. 

##### Styles

`VisualGroup3D` has a `styleSheet` property that can optionally define styles at the Group's 
level. Styles are applied to child (descendant) objects using `styles: List<String>` property defined 
in `VisualObject`. 


### dataforge-vis-spatial-gdml

GDML bindings for 3D visualization (to be moved to gdml project).


### dataforge-vis-jsroot

Some JSROOT bindings. 

Note: Currently, this part is experimental and put here for completeness. This module may not build.


## Demonstrations

The `demo` module contains several example projects (demonstrations) of using the `dataforge-vis` framework:

### Simple Example - Spatial Showcase

Contains a simple demonstration with a grid including a few shapes that you can rotate, move camera, and so on.
Some shapes will also periodically change their color and visibility.

[More details](demo/spatial-showcase/README.md)

##### Example view:

![](doc/resources/spatial-showcase.png)


### Full-Stack Application Example - Muon Monitor Visualization

A full-stack application example, showing the 
[Muon Monitor](http://npm.mipt.ru/projects/physics.html#mounMonitor) experiment set-up.

[More details](demo/muon-monitor/README.md)

##### Example view:

![](doc/resources/muon-monitor.png)


### GDML Example

Visualization example for geometry defined as GDML file. 

[More details](demo/gdml/README.md)

##### Example view:

![](doc/resources/gdml-demo.png)
