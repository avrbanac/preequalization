# Pre-equalization (test) application

> [back to parent readme](../README.md)

Small helper utility FX based application - to allow for an easy PreEq library test and output comparison to a third 
party PreEq tool.

# Official documentation

Basic information can be found in this [README](README.md) file.

## How to run

Since JavaFX is not a part of the JDK, additional dependencies are listed in app pom. But, there is an additional requirement for app to run
as it supposed to. Runtime libraries are also needed and there are more than one way to set this up. Maven plugin is also included in app
pom, and it enables for easy JavaFX usage. If used in this way, instead of running app from IDE directly, `mvn clean javafx:run` directive
should be used. Alternatively, runtime SDK can be downloaded and needed modules can be targeted via VM options. More information can be
found online [Getting started with JavaFX](https://openjfx.io/openjfx-docs/).

## Change log

Please see [parent CHANGELOG](../CHANGELOG.md) for more information on what has changed recently.

## Current version

Current module version is tracked in [parent pom.xml](../pom.xml) file.
Also, same version number is used for parent and both modules via revision reference.

## Author

**avrbanac**

