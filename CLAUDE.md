# CLAUDE.md

Guidance for Claude Code when working in this repository.

## What this is

A Java library that parses **DOCSIS 2.0 / 3.0 upstream pre-equalization (PreEq) strings** from cable modems and computes line-quality KPIs from them. The strings are collected over SNMP (`docsIfCmStatusEqualizationData`, `docsIf3CmStatusUsTable`, and the CMTS equivalents). A small JavaFX app is included to test the library visually. The project is MIT-licensed. The author says in the README that they are not a DOCSIS expert, and several algorithms are empirical or were reverse-engineered from third-party CableLabs tooling.

Scope is deliberately narrow: only the **24-tap, 1-coefficient-per-symbol** format is supported. DOCSIS 1.x (T/2, T/4 spacing) and 3.1 are out of scope.

## Build & run

Maven multi-module project, Java 11 target. The repo has no Maven wrapper, so you need `mvn` on PATH.

```bash
mvn clean install                 # build lib + app, run tests
mvn -pl lib test                  # lib tests only
mvn -pl lib test -Dtest=PreEqDataTest#testTDR   # single test method
cd app && mvn clean javafx:run    # run the JavaFX test app (lib must be installed first)
```

- The app needs the JavaFX runtime. Use `javafx:run`; launching `PreEqualizationApplication` directly from the IDE fails unless the JavaFX SDK modules are passed as VM options (see `app/README.md`).
- No surefire version is pinned. JUnit 5 tests run only with Maven 3.9+ defaults. With older Maven (surefire 2.12.x), the tests are silently skipped.
- `junit-jupiter-engine` is declared without `<scope>test</scope>` in `lib/pom.xml`, so it leaks onto the compile classpath of `lib` and `app`.

## Versioning & changelog

- The version is hardcoded in **four places** and must be bumped together: root `pom.xml`, `lib/pom.xml` (parent), `app/pom.xml` (parent **and** the `lib` dependency version). `${revision}` was removed in v0.3.1 because it caused build problems.
- Every change gets an entry in `CHANGELOG.md` (Keep a Changelog style; sections Added / Fixed / Removed / Tested). The entry goes under a new `## vX.Y.Z @avrbanac` header, and lib and app share one changelog.
- The open roadmap item is in `## Unreleased / TODO`: the correlation algorithm (convolution of IFFT with ICFR). `CorrelationBond` exists for it but is not used yet.

## Architecture

```
lib/  hr.avrbanac.docsis.lib
├── PreEqException          RuntimeException; static singleton instances per ErrorClass + String-wrapping ctor
├── struct/                 parsing + key metrics
│   ├── PreEqData           = PreEqHeader + PreEqMetrics + coefficients
│   ├── DefaultPreEqData    the only impl: 24 taps, ALL metrics computed eagerly in the constructor
│   ├── Coefficient
│   └── DefaultCoefficient  4 bytes -> (real, imag), 3- or 4-nibble 2's complement decoding
├── analysis/
│   ├── PreEqAnalysis       FFT -> ICFR (lazy + cached), TDR, Signature factory
│   ├── Signature           micro-reflection (dB), severity, MR delay from ICFR magnitude
│   ├── ChannelWidth        CW_US_3_2 / CW_US_6_4 (symbol rate, period, max delay), VoP=0.87, c
│   ├── MicroReflectionSeverity(+Threshold)  GOOD/MARGINAL/BAD × LOW/MEDIUM(CableLabs)/HIGH
│   ├── PreEqFFTInputFormat FIRST_TAP_FIRST_POINT (used, rotation 2) vs MAIN_TAP_MIDDLE
│   └── CorrelationBond     placeholder for future correlation work
└── util/
    ├── MathUtility         ParabolicInterpolation V1 (CableLabs doc) / V2 (3rd-party tool, default), FFT size, TDR speed factor
    └── ParsingUtility      hex<->bytes, input validation, metrics pretty-print

app/  hr.avrbanac.docsis.app   (JavaFX 11, FXML)
├── PreEqualizationApplication  entry point; loads /views/preeq-view.fxml
├── PreEqualizationController   single controller; onCalculateClick() drives everything
├── TapBarChart                 StackedBarChart subclass patching JavaFX's broken "negative" CSS class
├── ICFRLineChart               ICFR plot with MHz x-axis
├── TableCoefficient            table row POJO
└── PreEqAppConfiguration       window size/title/resource paths (constants)
```

### Data flow

`raw string` → `DefaultPreEqData(String)` (normalize: lowercase, strip `:` and spaces; validate 200 hex chars; header bytes per RFC 4546 = main tap index (must be 2..23), taps per symbol (must be 1), forward tap count (must be 24), reverse tap count (must be 0); decode coefficients; compute metrics) → `PreEqAnalysis(preEqData)` → `getInChannelFrequencyResponseMagnitude()` / `getTDR(channelWidth, nearTaps, onlyFar)` / `getSignature(channelWidth[, threshold])`.

### Key metrics (all on `PreEqMetrics`)
Energy values are `long`: MTE, MTNA, MTNE, preMTE, postMTE, TTE. Ratio values are `double` in dB: MTC, MTR, NMTER, preMTTER, postMTTER, PPESR, PPTSR. Field naming uses a type prefix: `lXXX` for long and `dXXX` for double.

## Conventions & gotchas

- **Tap index vs array index**: `getMainTapIndex()` and `Coefficient.getIndex()` are 1-based tap numbers. Use `index - 1` to access the list. Getting this wrong is the most common source of off-by-one bugs here (`PreEqAnalysis.getTDR` mixes both conventions on purpose).
- **Nibble encoding** is decided once per string in `DefaultPreEqData.is3NibbleEncoding`. A string uses 3-nibble decoding only if no coefficient's leading nibble is set; otherwise it uses 4-nibble decoding (CableLabs CM-GL-PNMP-V03 §6.2). The mask `0xF0` is applied to the upper byte of each value; until v0.4.1 it was `0xF000` against a sign-extended `byte`, which misread MTNA-32767 strings.
- **MTNA** (`calculateMTNA`) is empirical. It searches 2ⁿ−1 values from 511 upward within 0.05% of √TTE and falls back to the old ceil-log2 formula. Javadoc explains why.
- `DefaultPreEqData` throws on division-by-zero cases (postMTE == 0). PPTSR reads taps adjacent to the main tap, so a main tap at position 1 or 24 throws. All non-`PreEqException` errors are wrapped into `PreEqException`.
- The `PreEqException` constants are **shared singleton instances** with a fixed stack trace. Don't rely on their stack traces. Add new error types as an `ErrorClass` entry plus a static constant.
- TDR sanitizes tilt or inverted-concavity results to `1 × speedFactor` rather than returning negative or huge values.
- `Signature` delay: if only one peak is found, the delay is reported as `(int)(1/symRate*1000) - 1` ns when MR > −18 dB, otherwise 0.
- The code style keeps `final` on all parameters and locals where possible. Constructor parameters go one per line. Every public method and class gets full Javadoc; use `{@inheritDoc}` on impls. A javadoc-lint clean state was reached in v0.3.5, so keep it clean.
- Line width is about 140 characters.
- The logging API is SLF4J, backed by log4j2 (`log4j2.xml` in main, `log4j2-test.xml` with a bare `%msg%n` pattern for tests).

## Tests

All lib tests live in `PreEqDataTest` and are **data-driven from `TestBook`**. `TestBook` is a static registry of:
- `TestPreEqStructure` (built with a Builder): input string, expected header, long metrics, dB metrics rounded to 3 decimals, TDR rounded to 2, the full 32-point ICFR array rounded to 4, and the signature (MR rounded to 3, severity name, delay),
- `TestCoefficientStructure`: raw bytes → expected real/imag,
- `TestParabolicInterpolation`: three points → expected x (tests V1 only; TDR tests use V2).

To add coverage, add a new entry to `TestBook` instead of writing a new test method.

**Source of truth.** Correctness is judged against the CableLabs documentation in `docs/references` (mainly CM-GL-PNMP-V03) and physics, not against past outputs. The existing real-world `TestBook` values came from the code itself or from a third-party pre-eq tool that is known to be buggy (non-deterministic results). Treat them as **unverified regression values**; the TDR value is explicitly marked as such. Prefer synthetic test strings with known ground truth (e.g. a single echo of known level and delay). An expected value may change when the new value is backed by a doc section or a physics argument; cite it in the CHANGELOG.

The app has no tests.
