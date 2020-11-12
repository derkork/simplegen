# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.1.0] 2020-11-11
### Added
* SimpleGen now allows for providing data in different formats than YAML. To do so, specify the mime type of the data format you are using in your data section like this:
  ```yaml
  - data:
    - includes: "**/*.toml"
      mimeType: application/toml
    
  ```
  If you leave out the mime type, SimpleGen will use YAML as the default. You can now use [TOML](simplegen-dataformat-toml/README.md) and [XML](simplegen-dataformat-xml/README.md) files to provide data for SimpleGen.
* There is now an API available for extending SimpleGen. For now this API only allows you to write parsers for custom data formats. 
* A new built-in filter for accessing environment variables named `env` is now available. You can use it like this:
  ```jinja2
  {{ 'HOME' | env }}
  ```
### Changed
* Updated the project to Kotlin 1.4
* Replaced Spek testing framework with Kotest as Kotest seems to be a lot more mature. This also fixed that the tests were not running in then Maven build when using Spek.

### Fixed
* Fixed a packaging error for the maven plugin and standalone jar that would prevent custom javascript filters from working when they used the `Regex` class.

### Security
* Update `ant` and `guava` dependencies to mitigate security issues.

## [2.0.0] - 2020-02-10
### Added
* Due to the migration to GraalVM there is now a `console.log` command available inside the JavaScript filters which can be used to output debug information ([#19](https://github.com/derkork/simplegen/issues/19))
### Changed
* **Breaking Change**: SimpleGen now uses GraalVM instead of Nashorn for evaluating custom script filters ([#18](https://github.com/derkork/simplegen/issues/18)). For most filters this should be a drop-in-replacement however some filters may not work anymore. See the [Nashorn Migration Guide](https://github.com/graalvm/graaljs/blob/master/docs/user/NashornMigrationGuide.md) for details. Nashorn compatibility mode is enabled.
* **Breaking Change**: The second argument for script filters is no longer an instance of `com.hubspot.jinjava.interpret.JinjavaInterpreter` but rather a function which allows you to resolve template variables: 
    ```javascript
    // old
    function myFilter(input, interpreter, args) {
        var templateVariable = interpreter.context['variableName'];
    }
    
    // new
    function myFilter(input, resolve, args) {
        var templateVariable = resolve('variableName');
    }
    ```
### Security
* Update `jackson` libraries to mitigate various CVEs.
* Update `jinjava` library to mitigate CVE-2018-18893.

## [1.0.8] - 2018-12-12
### Added
* Incremental code generation ([#14](https://github.com/derkork/simplegen/issues/14))
    
### Changed
* Better error messages on missing files ([#2](https://github.com/derkork/simplegen/issues/2), [#7](https://github.com/derkork/simplegen/issues/7))
* Better support for multiple functions in a single script file ([#17](https://github.com/derkork/simplegen/issues/17))
* Updated documentation for the new features.



## [1.0.7] - 2017-06-30
### Changed
* JavaScript function names no longer need to be lower case ([#8](https://github.com/derkork/simplegen/issues/8))

## [1.0.6] - 2017-04-05
### Added
* JavaScript filters now can return null values

## [1.0.5] - 2017-03-27
### Added
* Support for custom JavaScript filters

## [1.0.4] - 2017-03-20
### Added
* It is now possible to configure template engine.

## [1.0.3] - 2016-10-25
* Initial public release
