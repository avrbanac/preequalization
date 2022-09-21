# ChangeLog

All notable changes to 'Pre-equalization project' will be documented in this file.

Updates should follow the [Keep a CHANGELOG](http://keepchangelog.com/) principles.

## Unreleased

Lib part is missing TDR calculation and group correlation analysis.

## v0.3.1 @avrbanac

### Fixed
- moved all specific chart logic to custom classes (graph classes)
- fixed generics to types that make sense for pre-eq data

### Removed
- revision from parent pom (kept making build problems) versions are now manually maintained

## V0.3.1 @avrbanac

### Fixed
- moved all chart logic from controller to the specific chart class
- simplified chart structure (now generics are defined since type of data is well known)
- line chart x-axis is now properly labeled

## v0.3.0 @avrbanac - 20220921

App is now functional. It is intended for the use with default implementation of 24 tap pre-eq string. At this point it is unnecessary to
support scaling for large number of taps.

### Fixed
- now using custom linechart and custom stacked barchart (with additional plot elements) instead of original FX versions

## v0.2.3 @avrbanac - corrected app graphs

### Added
- graph coloring

### Fixed
- switched from barchart to stacked barchart so that dB tap graph can be shown in negative values all the way to the zero for the main tap
- fixed broken javafx stacked barchart so that it can correctly show negative values (explained in the FixedStackedBarChart class)
- metrics are shown in hBox with two columns (vBoxes)

## v0.2.2 @avrbanac - not a module anymore, app added

### Added
- complete app view with table (parsed coefficients), metrics, bar chart (taps), and line chart (ICFR)

### Removed
- module-info.java from project

## v0.2.1 @avrbanac - app WIP

### Added
- app FX skeleton with fxml support
- main view (WIP)

### Fixed
- package change from hr.avrbanac.preequalization to hr.avrbanac.docsis to follow groupId.artifactId naming convention

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