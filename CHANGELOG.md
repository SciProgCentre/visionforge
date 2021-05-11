# Changelog

## [Unreleased]
### Added
- Server module
- Change collector
- Customizable accessors for colors
- SphereLayer solid
- Hexagon interface and GenericHexagon implementation (Box inherits Hexagon)
- Increased the default detail level for spheres and cones to 32
- Simple clipping for Solids in ThreeJs

### Changed
- Vision does not implement ItemProvider anymore. Property changes are done via `getProperty`/`setProperty` and `property` delegate.
- Point3D and Point2D are made separate classes instead of expect/actual (to split up different engines.
- JavaFX support moved to a separate module
- Threejs support moved to a separate module
- \[Format breaking change!\] Stylesheets are moved into properties under `@stylesheet` key
- VisionGroup builder accepts `null` as name for statics instead of `""`
- gdml sphere is rendered as a SphereLayer instead of Sphere (#35)
- Tube is replaced by more general ConeSurface

### Deprecated

### Removed
- Primary modules dependencies on UI

### Fixed
- Version conflicts

### Security
