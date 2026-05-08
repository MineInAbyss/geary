# Changelog

All notable changes to this project between Minecraft versions will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/). We currently do not follow semver while
MAJOR is set to zero.

## [Unreleased]

### Added

- `geary-examples` module with some basic engine setup examples

### Changed

- Switched to our own small DI library for internal module and for addons, docs can be
  found [here](https://docs.mineinabyss.com/dependencies-kt/)
- `WorldScoped` is now the preferred receiver when writing extension functions. `WorldScoped.newScope()` can be used to
  register systems, observers, etc... and automatically unregister them when calling `close` on the returned scope
