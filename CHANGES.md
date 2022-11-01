# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [3.1.0] - 2022-11-01
### Added
* You can now use [CSV](simplegen-dataformat-csv/README.md) as an input data format.
* The Maven plugin now supports generating test sources. See the [README](simplegen-maven-plugin/README.md) for details (fixes [#44](https://github.com/derkork/simplegen/issues/44).
### Improved
* When a data file include definition yields no results, this will be printed as a warning. This is preferable to the previous behaviour where this was just silently ignored.
### Fixed
* When referring to data with a simple relative path that is outside the base directory, the path is now correctly resolved.
* SimpleGen will no longer crash when a JavaScript error has no detailed information.
* When the `nodes` property is missing or invalid, a more helpful error message is now printed. (fixes [#32](https://github.com/derkork/simplegen/issues/32)).

```yaml
transformations:
  - data:
      # this is now correctly parsed
      - ../data/data.yaml

```
## [3.0.0] 2022-05-31
### Added
* You can now specify inline data directly in `config.yml`. This is useful if you use multi-stage code generation (e.g. generate a `config.yml` and then run SimpleGen on the generated configuration) or if you just have a simple generation need and don't want to use extra data files to have all generation settings in one place.

  ```yaml
  transformations:
    - data:
      - inline:
          this: is
          inline: data
          it:
            - can 
            - be 
            - arbitrarily
            - nested: yaml         
  ```

  Note that inline data always needs to be a map. Inline data can be mixed with all other data input, so you can mix it with YAML files, TOML files or XML files.

### Changed
* **Breaking Change**: You can now configure nested interpretation in the template engine. With nested interpretation the template engine will re-evaluate the result of an expression until there is no more Jinja code in it. 

  ```jinja2
  {% macro build_expression(value) %}
  {{ '{{' }}{{ value }}{{ '}}'}} 
  {% endmacro %}
  
  {# we print #}
  -- {{ build_expression( 'test' ) }} --
  
  {# with nested interpretation off this yields: #}
  -- {{ test }} --
  
  {# with nested interpretation on, the {{ test }} will
   be re-evaluated again. Because no test variable is
   defined, this will yield: #} 
  -- --
  ```
  You can configure nested interpolation in `config.yaml` in the template engine settings:

  ```yaml
  templateEngine:
    # if true, expressions that yield jinja template code will be re-interpreted
    # until no more jinja template code is in them. Use with care.  
    # default false
    nestedInterpretationEnabled: true
  ```
  Before this setting was introduced, this was always enabled. I chose to disable it by default because it has a lot of sometimes puzzling side-effects and it has security implications if you run the code generator on input you do not have under control.

### Security
* Updated various library dependencies to mitigate security issues.

## [2.1.1] 2021-03-01
### Fixed
* Fixed a parse error when parsing TOML files having quotes in their string values.

### Security
* Updated the `jackson` and `ant` dependencies to mitigate security issues.

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
* Updated `ant` and `guava` dependencies to mitigate security issues.

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
* Updated `jackson` libraries to mitigate various CVEs.
* Updated `jinjava` library to mitigate CVE-2018-18893.

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
