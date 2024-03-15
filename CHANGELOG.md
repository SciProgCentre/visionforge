# Changelog

## Unreleased

### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

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
