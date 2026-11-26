# ChangeLog

All notable changes to 'Pre-equalization project' will be documented in this file.

Updates should follow the [Keep a CHANGELOG](http://keepchangelog.com/) principles.

## Unreleased / TODO
- correlation algorithm (convolution of IFFT with in channel frequency response)

## v0.5.0 @avrbanac - TDR / echo delay rework

Behaviour change: TDR distances and signature delays differ from v0.4.x. The new method was validated against ground-truth
synthetic echoes (`EchoChannelGenerator`); the old one was not and was wrong for echoes next to the main tap.

### Added
- `TdrMethod`: `BAND_LIMITED` (default) - sinc (Whittaker-Shannon) interpolation of the complex post-main taps with the main tap
  removed, fine-grid search, parabolic refinement on evenly spaced grid points; `CABLELABS_PARABOLIC` - CM-GL-PNMP-V03 6.6.4
  (three taps in dB, local peak required), kept for comparability
- `EchoEstimate`: echo delay in T, `isResolved()`, conversion to ns and to distance for a `ChannelWidth`
- `PreEqAnalysis.getEchoEstimate(...)`, `Signature.isDelayResolved()`
- `MathUtility.parabolicPeak`, `bandLimitedValue`, `sinc`
- ground-truth tests for echo delay (0.9-15 T, error within 0.15 T below 1.5 T and 0.08 T above; CableLabs method within 0.25 T)
  and for sub-symbol echoes (0.3-0.5 T must be reported as not resolved)

### Fixed
- TDR for echoes next to the main tap was ~2x too short (V2 interpolation over a 2-tap gap): e.g. a 1 T echo gave ~13 m instead of
  25.5 m. Real test string: 12.2 m -> 25.82 m
- signature delay was quantized to 32-point FFT bins (e.g. 430 ns for a 293 ns echo) and reported 0 for weaker echoes at 1 T; it now
  comes from the same echo estimate as TDR (real test string: 0 -> 198.0 ns)
- echoes too close to the main tap are now reported as not resolved (nominal 1 T, shown as "<=" in the app) instead of a falsely
  precise value

### Removed
- `MathUtility.ParabolicInterpolation` (V1 / V2); V1 survives as `MathUtility.parabolicPeak` with evenly spaced points only
- `Signature(double[], ChannelWidth[, threshold])` constructors; `Signature` now takes an `EchoEstimate`
- ICFR peak/valley delay heuristics in `Signature`

## v0.4.1 @avrbanac

### Fixed
- 3-nibble / 4-nibble encoding detection: leading nibble mask was applied to a sign-extended byte (`0xF000`), so only upper bytes
  >= 0x80 were detected; values like `0x7FFF` (MTNA 32767 modems) were decoded as 3-nibble (main tap became -1)
- TDR test expected value changed from 21.96 to 12.2; the old value came from the pre-v0.3.4 TDR algorithm and the test had
  been failing since v0.3.4. 12.2 is a regression value only (current output), NOT a verified result; the TDR method itself
  is under review against CableLabs CM-GL-PNMP-V03

### Changed
- correctness reference is now CableLabs documentation and physics (see `docs/references`); existing test values are treated as
  unverified legacy values

- non-hex characters were silently parsed (`Character.digit` returns -1, it does not throw), so `STRING_NOT_BYTE_REPRESENTATION`
  was never raised; now validated explicitly (also odd length)
- header byte 4 is the number of reverse taps (RFC 4546), not a reserved byte; non-zero value was silently accepted, now rejected
  with new `REVERSE_TAP_COUNT_NOT_SUPPORTED` (3003)
- main tap index was not range-checked; values outside 2..23 failed with internal index-out-of-bounds messages, now rejected with
  new `MAIN_TAP_INDEX_OUT_OF_RANGE` (3004)

### Added
- pre-eq decoding test (`TestPreEqDecoding` in `TestBook`) with synthetic 3-nibble, 4-nibble and MTNA 32767 strings
- invalid pre-eq string test (`TestPreEqError` in `TestBook`) covering every header field, length and non-hex input
- `EchoChannelGenerator` (test scope): builds pre-eq strings with known ground truth from a single-echo channel model (raised
  cosine roll-off 0.25, 24 T-spaced taps, CM-SP-PHYv3.0 6.2.16-17); cross-checked against an independent Python model
- ground-truth micro-reflection test (`TestEcho` in `TestBook`): 63 synthetic echoes (1.5-5.7 T, -15/-21.5/-30 dB, 3 phases);
  MR level within 0.5 dB and CableLabs severity

### Tested
- with ground-truth strings: MR level is accurate (within ~0.1 dB) for 1.5-5.7 T; reads ~1.3 dB low at 8 T; TDR is wrong for
  echoes near the main tap (~0.5 T reported for 0.6-1.25 T); Signature delay is coarse (e.g. 430 ns reported for a 293 ns echo).
  TDR / delay to be reworked in v0.5.0
- reference documentation index (`docs/references`)

## v0.4.0 @avrbanac

### Fixed
- delay calculation

### Tested
- signature with its delay calculation (test base of 1k pre-eq strings)

## v0.3.7 @avrbanac - adding Signature MR delay feature

### Added
- delay for Signature MR

### Fixed
- small changes to Signature class structure
- update to other classes that use Signature
- updated test classes
- updated javadoc for changed classes

## v0.3.6 @avrbanac

### Fixed
- more precise ChannelWidth javadoc
- renamed MinMaxValue Signature's inner class to SignatureArray and added new methods (createTranspondedArray)

### Removed
- unnecessary import from App controller

## v0.3.5 @avrbanac

### Added
- javadoc for packages
- javadoc to methods that were missing documentation
- javadoc to test classes

### Fixed
- organized parabolic interpolation in a way that both algorithms are available for calculation
- app updated to address changes to lib
- javadoc lint errors


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