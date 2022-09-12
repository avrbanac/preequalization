# ChangeLog

All notable changes to 'Pre-equalization library' will be documented in this file.

Updates should follow the [Keep a CHANGELOG](http://keepchangelog.com/) principles.

## Unreleased

At the moment, both library and application are under development.

## v0.1.0 @avrbanac - WIP

### Added
- documentation skeleton
- common dependencies (log and test)
- general structure and lib package root
- lib log configuration (resources)
- pre-eq exception class and struct package with structure interfaces
- default implementation classes and main parsing logic
- key metrics calculation
- analysys package with fft

### Deprecated
- nothing

### Fixed
- switched from universal 3 nibble coefficient decoding to more complex decoding for modems that use higher than 2047 MTNA

### Removed
- nothing

### Tested
- default coefficient creation
- default pre-eq data creation with structure output
- alanysis for pre-eq (FFT)

### Security
- nothing