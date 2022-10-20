# ChangeLog

All notable changes to 'Pre-equalization project' will be documented in this file.

Updates should follow the [Keep a CHANGELOG](http://keepchangelog.com/) principles.

## Unreleased / TODO

- correlation algorithm


## v0.3.4 @avrbanac

### Added
- new parabolic interpolation method in MathUtility (gives better results)
- new method to Coefficient interface (getEnergyRatio - since the one from before is in fact nominal variant)
- documentation info on how to run APP (JavaFX module)

### Fixed
- small changes to app to address lib changes
- renamed Coefficient getEnergyRatio method to getNominalEnergyRatio (since it is calculated with MTNE and not with TTE)
- switched to simpler icons (looks a bit better with small ones)
- TDR calculation fixed (used coefficient imaginary part instead of calculated getEnergyRatio value)
- sanitized TDR results for negative tilt case and inverted concavity case

## v0.3.3 @avrbanac

### Added
- signature structure as basis for the correlation calculation (with micro-reflection and severity)
- micro-reflection severity enums
- app icons (64x64, 32x32, 16x16 sizes)
- some quality of life testing builder methods

### Tested
- new calculations

## v0.3.2 @avrbanac

### Added
- channel width enumeration for TDR analysis
- rounding for app data (table and key metrics)
- TDR calculation (time-domain reflectometry)
- math utility helper class
- selector for channel width
- TDR calculation added to application as another metrics datum
- ICFR now shows correct x-axis values
- now there is a default pre-eq string already entered (faster testing purposes)

### Fixed
- default selection in app is focused on calculate button
- removed additionally added, overlapping graphical elements from custom charts
- MTNA calculation upgraded for cases that went one scale higher than the real values should (new algorithm added and documented)
- unit tests are organized via BookTest class, making it easy to add additional testing by simply adding new tests (values) to arrays

### Tested
- new way of testing: there is a test book with all test data defined in one place
- it is now very easy to add additional test structures to test book using predefined structures

### Removed
- hardcoded values for window definition (width, height, title...); moved to separate configuration class (maybe do the properties file?)

## v0.3.1 @avrbanac

### Fixed
- moved all chart logic from controller to the specific chart class
- simplified chart structure (now generics are defined since type of data is well known)
- line chart x-axis is now properly labeled

### Removed
- revision from parent pom (kept making build problems) versions are now manually maintained

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