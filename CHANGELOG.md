# Changelog

## [Unreleased]
### Added
- Server module
- Change collector

### Changed
- Vision does not implement ItemProvider anymore. Property changes are done via `getProperty`/`setProperty` and `property` delegate.
- Point3D and Point2D are made separate classes instead of expect/actual (to split up different engines.
- JavaFX support moved to a separate module
- Threejs support moved to a separate module

### Deprecated

### Removed
- Primary modules dependencies on UI

### Fixed
- Version conflicts

### Security
