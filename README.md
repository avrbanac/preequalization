# Pre-equalization (PreEq) - under development!
DOCSIS 2.0 / 3.0 pre-equalization library for parsing and processing of the CM upstream PreEq strings. These hex strings
are converted into energy coefficients (taps) so that line quality KPIs can be calculated.

Together with the library there is also a small java FX based application to test parsing and calculated values (time
and frequency domain graphs with metrics table).

The idea behind the whole preEq library is to analyze preEq strings since they carry the information on what the device needs to do (how it
needs to adjust) to account (and thus mitigate) for all the linear and sometimes non-linear distortions. With knowing the exact changes the
device needs to do with its "setup", it is possible, in a way, to go in a reverse direction and work out the line anomalies.

Calculated values and KPIs need to be presented in a more understandable way than just as a plain number values. This presentation is beyond
this project, but app module accompanying preEq library can give a crude idea on how to do that.

## Official documentation

Basic information can be found in this [README](README.md) file.
Library contains detailed javadoc.
Additional readme files accompany each module.

## Change log

Please see [CHANGELOG](CHANGELOG.md) file for more information on what has changed recently.
Both lib and app changes will be kept in the same changelog.

## Current version

Current version is tracked in [pom.xml](pom.xml).

## Disclaimer (and reasoning behind this)

I'm, by no means, a DOCSIS expert. All needed information to write this library I found online, and with a little 
stubbornness, managed to put together. From what I read, these calculations are both valid for DOCSIS 2.0 and 3.0
devices, but I'm not sure if library is usable for DOCSIS 3.1 standard. Also, there really was no need for me to support
less than 24 energy tap calculations (for DOCSIS 1.0) or more than 24 for that matter. This project's scope is limited 
only to pre-equalization strings so parsing support for some other eq structure is omitted by design. If at any time 
need arises for some other eq structure parsing and processing, lib can be extended. Default struct implementations can 
be renamed to better describe internal structure, while factory classes or builders can be added to accommodate easier 
eq string parsing.

I decided to write this library and make it available on my private GitHub under the MIT licence since there never seems
to be enough time to write it at work.

## Project structure

DOCSIS pre-equalization project is a two-part project:

- [ ] pre-equalization library
- [ ] pre-equalization application (for testing purposes)

### Modules

|       Module       | Type        | Readme                     | Description                                               |
|:------------------:|-------------|----------------------------|-----------------------------------------------------------|
| [lib](lib/pom.xml) | library     | [PreEq-lib](lib/README.md) | Pre-equalization library with all needed parsing and math |
| [app](app/pom.xml) | application | [PreEq-app](app/README.md) | Pre-equalization test application for the PreEq library   |

## Author

**avrbanac**

## License

![GitHub](https://img.shields.io/github/license/avrbanac/preequalization)
