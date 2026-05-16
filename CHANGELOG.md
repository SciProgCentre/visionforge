# Changelog

## Unreleased

### Added
- Pure Kotlin CSG implementation by https://github.com/mon-key1
- Plotly implementation and demo for Kotlin/Wasm
- Wasm targets for plotly-kt and visionforge-core/solid
- Time (Instant) based TraceValues in Plotly

### Changed
- Order of arguments in Plotly-kt for js plotDiv functions

### Deprecated

### Removed

### Fixed
- Plotly-kt js demo

### Security

## 0.5.1 - 2026-01-10

### Changed

- Kotlin 2.3

### Fixed

- Fix DSLMarker annotations for builders
- Flaky test
- Fix the problem where property listeners do not react on property child node changes
- Plotly plot title now writes proper field

## 0.5.0 - 2025-03-21

### Added

- Plotly refactored on top of visionforge/server. Now it is called `plotly-kt`. Version follows general VisionForge version.

### Changed

- Simplified Vision and VisionGroup logic. Observation logic moved out.
- Use `Name` for child designation and `Path` for tree access
- The default intensity for AmbientLight is 1.0. The intensity scale for Three-js is 3.0. 

### Removed

- VisionChildren and VisionProperties

### Fixed

- Vision server now automatically switches to WSS protocol for updates if incoming protocol is HTTPS
- Issue with track removal in muon monitor demo.

## 0.4.2 - 2024-06-06

### Added

- Moved plotly-kt repo inside this one

### Changed

- Kotlin 2.0
- DataForge 0.9

## 0.4.1 - 2024-03-15

### Changed

- VisionProperties `flowChanges()` -> `changes`

### Fixed

- Bug with ElementVisionRenderer having the same name
- Plotly update problem

## 0.4.0 - 2024-02-16

### Added

- Added compose-mpp rendering.

### Changed

- **Breaking API** Move vision cache to upper level for renderers to avoid re-creating visions for page reload.
- **Breaking API** Forms refactor.
- **Breaking API** Migrated from React to Compose-html.
- **Breaking API** changed js package for `visionforge-core` to `space.kscience.visionforge.html` to avoid mixing html and generic parts.

## 0.3.0 - 2023-12-23

### Added

- Context receivers flag
- MeshLine for thick lines
- Custom client-side events and thier processing in VisionServer
- Control/input visions

### Changed

- Color accessor property is now `colorProperty`. Color uses non-nullable `invoke` instead of `set`. 
- API update for server and pages
- Edges moved to solids module for easier construction
- Visions **must** be rooted in order to subscribe to updates.
- Visions use flows instead of direct subscriptions.
- Radical change of inner workings of vision children and properties.
- Three package changed to `three`.
- Naming of Canvas3D options.
- Lights are added to the scene instead of 3D options.

### Fixed

- Jupyter integration for IDEA and Jupyter lab.

## 0.2.0

### Added

- Server module
- Change collector
- Customizable accessors for colors
- SphereLayer solid
- Hexagon interface and GenericHexagon implementation (Box inherits Hexagon)
- Increased the default detail level for spheres and cones to 32
- Simple clipping for Solids in ThreeJs
- Markdown module
- Tables module

### Changed

- Vision does not implement ItemProvider anymore. Property changes are done via `getProperty`/`setProperty` and `property` delegate.
- Point3D and Point2D are made separate classes instead of expect/actual (to split up different engines.
- JavaFX support moved to a separate module
- Threejs support moved to a separate module
- \[Format breaking change!\] Stylesheets are moved into properties under `@stylesheet` key
- VisionGroup builder accepts `null` as name for statics instead of `""`
- gdml sphere is rendered as a SphereLayer instead of Sphere (#35)
- Tube is replaced by more general ConeSurface
- position, rotation and size moved to properties
- prototypes moved to children
- Immutable Solid instances
- Property listeners are not triggered if there are no changes.
- Feedback websocket connection in the client.

### Removed

- Primary modules dependencies on UI

### Fixed

- Version conflicts
