# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
## [5.1.0] 2022-12-19
### Added
- It is now possible to use Jinja2 expressions for selecting the nodes to process in a transformation. Until now only JSONPath was possible. With Jinja2 expression and the ability to use JavaScript filters it is now possible to select nodes in ways that were not possible with a JSONPath (e.g. only odd/even nodes, comparison with arbitrary injected values ,etc.) or to transform the selection:

  ```yaml
  transformations:
    - data:
        - data.yaml
      template: template.j2
      nodes:
        # take all strings in the "nodes" array and prefix them
        # with the string "test-"
        expression: "data.nodes | prefix('test-')"
        type: jinja2
  
      outputPath: '{{ node }}.txt'
  ```
- The Maven plugin now has a standalone goal, so you can invoke SimpleGen via Maven without a `pom.xml` file. This is useful for example when you want to use SimpleGen in a CI/CD pipeline. The standalone goal is called `simplegen:generateStandalone` and can be invoked like this:

  ```bash
  mvn com.ancientlightstudios:simplegen-maven-plugin:5.1.0:generateStandalone
  ```
  By default, this looks for a `config.yml` in the current folder. You can override configuration settings using system properties, e.g.:

  ```bash
  mvn com.ancientlightstudios:simplegen-maven-plugin:5.1.0:generateStandalone \
   # name of the configuration file (default: config.yml)
   -Dsimplegen.configFileName=myconfig.yml \
   # path to the source folder (default: .)
   -Dsimplegen.sourceDirectory=/path/to/source \
   # path to the output folder (default: .)
   -Dsimplegen.outputDirectory=/path/to/output \
   # whether to re-generate files that have no changed input (default: false)
   -Dsimplegen.forceUpdate=true
  ```
### Improved
- SimplegGen now gives better error/warning messages for a variety of cases.

## [5.0.0] - 2022-11-28
### Changed
- **Breaking change**: The [HTML](simplegen-dataformat-html/README.md) parser no longer tries to merge parsed HTML files as this doesn't make sense for HTML files. Instead, it will return a list of parsed HTML files. This is a breaking change because the output of the HTML parser is now a list instead of a single object. 

### Added
- The HTML parser can now add the nested HTML subtree to the parsed data under the `@nestedHtml` property. This is useful if you want to get the full text of an HTML element including all nested elements. This is disabled by default and can be enabled by setting the `extractNestedHtml` option to `true`.

## [4.0.1] - 2022-11-28
### Fixed
- Fixed a dependency issue that caused the Maven plugin to fail in SimpleGen 4.0.0.


## [4.0.0] - 2022-11-25
### Added
- Added support for parsing [HTML](simplegen-dataformat-html/README.md) files.

### Changed
- **Breaking change**: The `resultPath` configuration option of the CSV parsing module has moved up one level and is now available for all data formats. This allows you to control where parsed data is mounted in the data tree:
  ```yaml
  # before
  data:
    - includes: some_data.csv
      mimeType: text/csv
      parserSettings:
        resultPath: some_data
  
  # after
  data:
    - includes: some_data.csv
      mimeType: text/csv
      resultPath: some_data
  ```
### Fixed
- Fixed a problem where the template engine could not load error messages when being run in the Maven plugin. This would lead to a less than helpful error message when a template error occurred.


## [3.1.1] - 2022-11-02
### Added
- The CSV extension now supports the `text/csv` mime type as well.

### Fixed
- The CSV extension now is also included with the Maven plugin.
- If the CSV contains an UTF-8 byte order mark (BOM), it is now automatically removed instead of breaking the first CSV header. You can disable this behavior by setting `stripBom` to `false` in the configuration.

## [3.1.0] - 2022-11-01
### Added
* You can now use [CSV](simplegen-dataformat-csv/README.md) as an input data format. **Note:** Due to a packaging error this was not included with the Maven plugin in 3.1.0. Please use 3.1.1 instead.
* The Maven plugin now supports generating test sources. See the [README](simplegen-maven-plugin/README.md) for details (fixes [#44](https://github.com/derkork/simplegen/issues/44)).
### Improved
* When a data file include definition yields no results, this will be printed as a warning. This is preferable to the previous behaviour where this was just silently ignored.
### Fixed
* When referring to data with a simple relative path that is outside the base directory, the path is now correctly resolved.

```yaml
transformations:
  - data:
      # this is now correctly parsed
      - ../data/data.yaml

```
* SimpleGen will no longer crash when a JavaScript error has no detailed information.
* When the `nodes` property is missing or invalid, a more helpful error message is now printed. (fixes [#32](https://github.com/derkork/simplegen/issues/32)).

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
