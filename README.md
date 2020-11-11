# SimpleGen -  A simple yet powerful general-purpose code generator

![GitHub build status](https://img.shields.io/github/workflow/status/derkork/simplegen/Build%20&%20Test)
![Maven Central](https://img.shields.io/maven-central/v/com.ancientlightstudios/simplegen)
![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/derkork/simplegen)

SimpleGen is a simple yet powerful general-purpose code generator. It is not specialized to any language,
so whatever your target language is (may it be Java, Kotlin, C#, Ruby, JavaScript, HTML, CSS, etc.) - if it is text then 
this generator should be able to create it. SimpleGen has been used successfully in several projects to help create 
repetitive or boilerplate code (e.g. JPA entity definitions or JavaScript rest clients), test data and even static 
documentation websites. 

## How SimpleGen works

SimpleGen takes data in various formats and runs this data through a Jinja2 template to generate output files:


```ditaa
    +------------+
    |            +-+
    |    Data    | |
    |  (various  | | +----+
    |  formats)  | |      |                            +------------+
    +------------+ |      |     +-------------+        |            +-+
      +------------+      +---> |             |        |   Output   | +-+
                                |  SimpleGen  +------> |   (Text)   | | |
                          +---> |             |        |            | | |
    +------------+        |     +-------------+        +------------+ | |
    |            |        |                              +------------+ |
    |  Template  | +------+                                +------------+
    |  (Jinja2)  |
    |            |
    +------------+
```



## Requirements

* Java 8 or later
* _Optional_: Maven 3 if you'd like to use the maven plugin.

## Usage

The generator can be used standalone or as a Maven plugin. In both cases it expects an input structure that looks like this:
 
* ``config.yml`` - the configuration file
* ``data.yml`` - a data file (optional, can also be in other formats, you can choose any name you like)
* ``template.j2`` - at least one template

## Running through the command line
To use the generator through the command line, [download the latest version](https://github.com/derkork/simplegen/releases/latest) from GitHub. Then you can invoke the code generator by running:

```
java -jar simplegen-aio-<version>.jar \
  --sourceDirectory <path to input structure> \
  --outputDirectory <path for generated files>
```

This will read the input structure from the input directory, generate the code and place the generated files in the output directory.  

## Running with Maven

Simply add the [Maven plugin](simplegen-maven-plugin/README.md) to your build plugins:

```xml
    <build>
        <plugins>
            ...
            <plugin>
                <groupId>com.ancientlightstudios</groupId>
                <artifactId>simplegen-maven-plugin</artifactId>
                <version><!-- 
                     see badge at the top of the github 
                     page for the current release version 
            --></version>
                <executions>        
                    <execution>
                        <id>generate</id>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>generate</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            ...
        </plugins>
    </build>
```

The plugin expects the configuration files to reside in `src/main/simplegen` and will write its output to 
`target/generated-sources/simplegen`.

#### Incremental generation

Sources are generated incrementally. This means that the generated files
are only recreated when the input for them (e.g. templates, data, scripts) has changed. This avoids
unnecessary re-compilation of generated code when the generated code hasn't actually changed.

If you want to re-generate all code no matter whether or not the input has changed, use the `--force`
command line option or the `<forceUpdate>true</forceUpdate>` Maven setting.

### Data

The data is a very important part of the workflow as it is the basis on which all templates are processed. Data is
parsed from data files into a map-like tree structure. You can use multiple source files for your data. SimpleGen
parses each file into a map-like tree structure and then merges all the trees into a single tree. This final tree
is the input for your templates.

Data can be given in various formats, the default being YAML:

```yaml
settings:
  - name: fooCount
    description: How much foo you need.
    type: int
    min: 1
    max: 100

  - name: barUrl
    description: URL to the bar system.
    type: String
```

Again, the data file is totally free-form, you can structure it any way that fits your needs. There are no special keywords that the generator acts upon, it's just a structured data file that drives code generation with your templates. E.g. if you want to generate code for settings, put in some settings. If you want to generate a static website, put in your content, etc. 

In addition to YAML the following data formats are supported:

* [TOML](simplegen-dataformat-toml/README.md)
* [XML](simplegen-dataformat-xml/README.md)


Now that we have the data in place, we can start working on the template.

### Templates
  
The templates control what output is produced. Templates are written in the Jinja2 template language. You can configure SimpleGen to run a template for a series of nodes in your YAML structure. In this example we configure SimpleGen to generate a settings Java class based on the settings we created in our YAML data and some documentation for the end user. 

```java
package com.example.settings;

public class ApplicationSettings {
    
    {#  Create the fields for the settings. #}
    {% for setting in data.settings %}

    {# We can create a Javadoc from the description. #}
    /**
     * {{setting.description}}
     */
    {# The field itself #}
    private {{setting.type}} {{setting.name}};

    {# a getter, we use the built-in case-filter to convert the settings name to a method name #}
    public  {{ setting.type }} get{{ setting.name | case('lower-camel', 'upper-camel') }}() {
        return this.{{setting.name}};
    }

    {# a setter #}
    public void set{{ setting.name | case('lower-camel', 'upper-camel') }}({{ setting.type }} value) {
        this.{{setting.name}} = value;
    }
    {% endfor %}
}
```

The template creates a java class with the defined settings each of them getting a field a getter and a setter. You could also generate code that reads settings from a file and validates them for the constraints given (e.g. required files, min and max size, etc.) but I left this out for clarity.

Now in addition we would like to have some documentation for the settings. So for each setting we would like to create an HTML file with a description of the settings. In a real scenario you will probably have all in one file, but I want to show how you can use the same data to generate a single file and multiple files.

```html
<html>
<body>
<h1>{{node.name}}</h1>
{{node.description}}

This setting is of type <code>{{ node.type }}</code>.
</body>
</html>
```
This is probably going to look very ugly, but I think it gets the point across. In these templates you see two variables that are being used:

* `node` - when you run SimpleGen you can configure that for each entry of a list in your data a copy of a certain template should be rendered. If you do this, the `node` value will contain the current entry in the list. E.g. for our HTML example, we would generate a copy of this template for each setting in our `settings` list.
* `data` - this contains the whole merged data tree from all scanned data files. This can be useful if you have global information  that you want to share across templates.
  
You can use every feature of the Jinja2 language including macros, includes, etc. When you include things remember that all paths must be specified relative to the ``config.yml`` file. SimpleGen also defines some additional filters on top of the Jinja2 built-ins that are quite useful. See the _Advanced_ section below for details on these. 


### Configuration

Now that we have data and a template, the last remaining step is to tell SimpleGen how to process these. You do this inside the `config.yml` file. There you can define a series of transformations that SimpleGen should perform. Each transformation will read in data from one or more files and then apply this data to a template for a subset of the nodes from the parsed data: 

```yaml
# You can have multiple transformations.
transformations:
    # From where to pull the data. You can pull more than one file, in which case their contents get merged.
    # Paths are relative to the config.yml file unless you specify an absolute path.
  - data: 
      # Fetches data from data.yml
      - data.yml
      # Also ant style file selection is supported, this fetches data from all YAML files below the
      # folder where config.yml is located (including config.yml)
      - **/*.yml
      # If you want more control you can specify includes and excludes.
      # E.g. if you don't want to have config.yml as part of your data 
      # you can exclude it:
      - includes: **/*.yml
        excludes: config.yml
        basePath: .
      # It is usally a good practice to put your data files into a subfolder, so you 
      # don't need to exclude the config.yml.
      - data/**/*.yml
      # Starting from version 2.1.0 SimpleGen supports additional data formats. You can specify a mime type 
      # if you want to use a different format than yaml. If no mime type is specified, yaml is assumed.
      - includes: **/*.toml
        mimeType: application/toml
      
    # Which template should be used to render the data. Specify the path relative to the config.yml file.  
    template: settings-class.java.j2

    # For which nodes in the data should the template be executed. This is a JsonPath.
    # Note how we use $ here, which means the root node. Since this is only a single node
    # SimpleGen will only generate a single file in this transformation.
    nodes: $
    # What should be the output path of the generated file. 
    outputPath: "com/example/settings/ApplicationSettings.java"
  
  # Now a second transformation for generating the HTML files
  - data: 
      - data.yml
    # Use the HTML template to generate the HTML documentation.
    template: documentation.html.j2
    # Repeat this for each of the settings in our list.
    nodes: $.settings
    # Because we create one file for each setting, we need to have
    # a unique file name for each generated output file. We can do this
    # by using an expression in the output path. This uses
    # node.name which is the name of the setting. 
    outputPath: "docs/{{ node.name }}.html"
```

You can use expressions in all fields of the configuration. So e.g. if you want to pull data from a folder set by a system property you can do it like this:

```yaml
# Assuming you have set the system property 'input.path' to '/some/path'

transformations:
  - data: 
      - includes: **/.yml
        basePath: "{{ 'input.path' | sp }}"  # will set the basePath to /some/path
```
You can also configure the template engine within `config.yml`. The template engine configuration is optional, if you leave it out, all values will be initialized with `false`. The configuration can be done globally or per transformation:

```yaml
# This is the global configuration. 
templateEngine:
    # if true the first newline after a template tag is removed 
    trimBlocks: true

    # if true tabs and spaces from the beginning of a line to the start of a 
    # block are stripped. (Nothing will be stripped if there are other
    # characters before the start of the block.)
    lstripBlocks: true

    # if true, macros are allowed to recursively call themselves. Be sure you
    # end the recursion at some point otherwise you may crash
    # with a stack overflow or just hang in an endless loop.
    enableRecursiveMacroCalls: true
    
transformations:
   - data: 
        - data.yml
     ...
     templateEngine:
        # This is a configuration for this single transformation, only.
        trimBlocks: false
        lstripBlocks: false
        enableRecursiveMacroCalls: false
```
### Advanced

Congratulations, you've made it through the documentation! This section contains some additional information that you may not need in each and every project but that is useful in some circumstances. 

#### Additional built-in filters

In addition to the standard filters, this package adds a `jsonpath` filter to the template engine, so you can use JSONPath to effectively select interesting substructures of your data:

```jinja2
{# find all private fields of the class #}
{% set private_fields = node | jsonpath("$.fields[?(@.visibility == 'private')]") %}

```

See the [JsonPath GitHub project](https://github.com/json-path/JsonPath) for a full documentation on how JsonPath works and what expressions you can use.

It is also possible to inject data through system properties using the `sp` filter:
  
```jinja2
# Assuming you have set a system property with -DsomeProp=someValue
{{ 'someProp' | sp }}  # will print someValue

```

Similarly, you can access environment variables using the `env` filter. Note that these variables are dependent on the operating system, so your templates may not be portable when relying on environment variables:

```jinja2
# will print the path of the user's home directory on Linux/OSX
{{ 'HOME' | env }}  
```


Finally a thing that is often required when generating code is case-changing of identifiers, so SimpleGen adds a custom filter for this as well. The syntax of this filter is:

```jinja2
case( <input case>, <output case> )
```

Supported case formats are:

* 'upper-camel' - FooBar
* 'lower-camel' - fooBar
* 'lower-hyphen' - foo-bar
* 'upper-underscore' - FOO_BAR
* 'lower-underscore' - foo_bar

```jinja2
{{ 'SomeString' | case('upper-camel', 'lower-hyphen') }} {# will print 'some-string' #}
{{ 'someString' | case('lower-camel', 'upper-camel') }} {# will print 'SomeString' #}
{{ 'some-string' | case('lower-hyphen', 'upper-camel') }} {# will print 'SomeString' #}

```

#### Custom filters

If you have specific needs for your project you can also write custom filters in JavaScript. A filter is simply a javascript function that receives objects and works on them. The functions will be executed inside the GraalVM scripting engine so you can use all of it's functionality (including full access to the Java API) in your scripts. 

```javascript
// a simple filter which multiplies the input
function times(input, resolve, arguments) {
    return input * arguments[0];
}
```

The three arguments of the function are:
* `input` - the object to filter
* `resolve` - a function that can be used to resolve template variables. See below for an example.
* `arguments` - the arguments given to the filter in the template (an array of strings)

You can access the template context in your custom filter. This is useful, if you would like to get access to some data in the `data` or `node` variables.

```javascript
// a simple filter which reads some value from the template context
function times(input, resolve, arguments) {
    var data = resolve('data');
    var node = resolve('node');

    // now do something useful with it.
}
```

To register your custom filter, add the following to your `config.yml`:

```yaml
customFilters:
   # Path to the script file containing the filter function, relative to config.yml
 - script: filters/myFilter.js
   # Name of the filter function. This is also the name that will be used for the filter inside the template engine.
   function: times

 - script: filters/moreFilters.js
   function:
    # You can also reference more than one function in the same file.
    - myOtherFilter
    - yetAnotherFilter

 # more custom filters..

```
  
Then you can use your filter inside your templates like this:

```jinja2
{{ 5 | times(2) }} {# prints 10 #}
```
   
You can find a larger example in [simplegen-maven-example!](simplegen-maven-example/).

