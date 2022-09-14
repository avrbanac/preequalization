# ChangeLog

All notable changes to 'Pre-equalization project' will be documented in this file.

Updates should follow the [Keep a CHANGELOG](http://keepchangelog.com/) principles.

## Unreleased

At the moment, application is under development.
Lib part is missing TDR calculation and group correlation analysis.

## v0.2.0 @avrbanac - 20220914

Lib is now functional for pre-eq analysis (both 24-tap and ICFR plotting). Check under unreleased section of the readme file for rest of the
functionalities (missing at the moment; part of the roadmap)

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
- supported 2 input formats for FFT analysis (main tap in half of the input array and sequential distribution of the taps from 0. pos)

### Removed
- nothing

### Tested
- default coefficient creation
- default pre-eq data creation with structure output
- analysis for pre-eq (FFT) - ICFR

### Security
- nothing