# Changelog

## [Unreleased]
### Added

### Changed
- Naming of Canvas3D options
- Lights are added to the scene instead of 3D options 

### Deprecated

### Removed

### Fixed

### Security

## [0.2.0]
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


### Deprecated

### Removed
- Primary modules dependencies on UI


### Fixed
- Version conflicts


### Security

