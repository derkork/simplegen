# SimpleGen -  A simple yet powerful general-purpose code generator

[![Build Status](https://travis-ci.org/derkork/simplegen.svg?branch=master)](https://travis-ci.org/derkork/simplegen)

SimpleGen is a simple yet powerful general-purpose code generator. It is not specialized to any language,
so whatever your target language is (may it be Java, Kotlin, C#, Ruby, JavaScript, HTML, CSS, etc.) - if it is text then 
this generator should be able to create it. SimpleGen has been used successfully in several projects to help create 
repetitive or boilerplate code (e.g. JPA entity definitions or JavaScript rest clients), test data and even static 
documentation websites. 

## How SimpleGen works

SimpleGen takes data in YAML format and runs this data through a Jinja2 template to generate output files:


```ditaa
    +------------+
    |            +-+
    |    Data    | |
    |   (YAML)   | | +----+
    |            | |      |                            +------------+
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

* Java 8
* Maven 3 if you'd like to use the maven plugin. 

## Usage

The generator can be used standalone or as a Maven plugin. In both cases it expects an input structure that looks like this:
 
* ``config.yml`` - the configuration file
* ``data.yml`` - a data file (you can choose any name you want, actually)
* ``template.j2`` - at least one template

## Running through the command line
To use the generator through the command line, [download the latest version](https://github.com/derkork/simplegen/releases/latest) 
from GitHub. Then you can invoke the code generator by running:

```
java -jar simplegen-<version>.jar --sourceDirectory <path to input structure> --outputDirectory <path for generated files>
```

This will read the input structure from the input directory, generate the code and place the generated files in the
output directory.  

## Running with Maven

Simply add the Maven plugin to your build plugins:

```xml 
    <build>
        <plugins>
            ...
            <plugin>
                <groupId>com.ancientlightstudios</groupId>
                <artifactId>simplegen-maven-plugin</artifactId>
                <version>1.0.6</version>
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

### Data

The data is a very important part of the workflow as it is the basis on which all templates are processed. There is
no specific data model (except that it has to be YAML) so you can choose a YAML structure that suits the needs of
your project. In this example let's create Java base classes from a model. The data file for this could look like this:

```yaml
myclasses:
  - package: com.ancientlightstudios.myapp
    name: SomeClass
    fields:
      - name: someField
        visibility: private
    	type: java.lang.String
    	
  - package: com.ancientlightstudios.myapp
    name: SomeOtherClass
    fields:
      - name: someField
        visibility: public
        type: boolean
```

Again, the data file is totally free-form, you can structure it any way that fits your needs. There are no special keywords
that the generator acts upon, it's just a structured data file. Now that we have the data in place, we can start working
on the template.

### Templates
  
The templates control what output is produced. Templates are written in the Jinja2 template language. You can configure 
SimpleGen to run a template for a series of nodes in your YAML structure. In this example we configure SimpleGen to run 
the following template for each of the children of the `myclasses` node.

```jinja2
package {{ node.package }};

{# We want to create a base class, so we take the class name and append 'Base' #}
protected abstract class {{ node.name }}Base {
    
    {#  Create the fields #}
    {% for field in node.fields %}

    {# The field itself #}
    {{ field.visibility }} {{ field.type }} {{ field.name }};


    {# a getter, we use the built-in case-filter to convert the field name to a method name #}
    {{ field.visibility }} {{ field.type }} get{{ field.name | case('lower-camel', 'upper-camel') }}() {
        return this.{{field.name}};
    }

    {# a setter #}
    {{ field.visibility }} void set{{ field.name | case('lower-camel', 'upper-camel') }}({{ field.type }} value) {
        this.{{field.name}} = value;
    }
    {% endfor %}
}
```

The template creates a base class with the defined fields and getters and setters ()which is not particulary useful but it 
should be sufficient to get the idea across). Inside of the template you have access to two variables:

* ``node`` - this is the part of the data that is currently being processed. In this example it would be the current node
  below the ``myclasses`` parent.
* ``data`` - this contains the whole data from all scanned data files. This can be useful if you have global information
  that you want to share across templates.
  
You can use every feature of the Jinja2 language including macros, includes, etc. When you include things remember that
all paths must be specified relative to the ``config.yml`` file. See the _Advanced_ section below for some of the built-in
features that you can use inside of the templates. 


### Configuration

Now that we have data and a template, the last remaining step is to tell SimpleGen who to process these. You do this
inside the `config.yml` file. Inside of that file you can define a series of transformations that SimpleGen should
perform. Each transformation will read in YAML data from one or more files and then apply this data to a template
for a subset of the nodes from the YAML data: 

```yaml
transformations:
  
    # From where to pull the data. You can pull more than one file, in which case their contents get merged.
    # Paths are relative to the config.yml file unless you specify an absolute path.
  - data: 
      # Fetches data from data.yml
      - data.yml
      # Also ant style file selection is supported, this fetches data from all YAML files below the
      # folder where config.yml is located (including config.yml)
      - **/*.yml
      # If you want more control you can specify includes and excludes. E.g. if you don't want to have config.yml
      # you can exclude it:
      - includes: **/*.yml
        excludes: config.yml
        basePath: .
      
    # Which template should be used to render the data. Specify the path relative to the config.yml file.  
    template: template.j2
    # For which nodes in the data should the template be executed. This is a JsonPath.
    nodes: myclasses
    # What should be the output path of the generated file. If you generate multiple files
    # you should use an expression here, to tell SimpleGen the output file name for each processed node.
    outputPath: "{{ node.package | replace('.', '/') }}/{{ node.name }}.java"
```

You can use expressions in all fields of the configuration. So e.g. if you want to pull
data from a folder set by a system property you can do it like this:

```yaml
# Assuming you have set the system property 'input.path' to '/some/path'

transformations:
  - data: 
      - includes: **/.yml
        basePath: "{{ 'input.path' | sp }}"  # will set the basePath to /some/path
```
You can also configure the template engine within `config.yml`. The template engine
configuration is optional, if you leave it out, all values will be initialized with `false`. The configuration
can be done globally or per transformation:

```yaml
# This is the global configuration. 
templateEngine:
    trimBlocks: true
    lstripBlocks: true
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

Congratulations, you've made it through the documentation! This section contains some additional information that
you may not need in each and every project but that is useful in some circumstances. 

#### Additional built-in filters

In addition to the standard filters, this package adds a `jsonpath` filter to the template engine, so you can use 
JSONPath to effectively select interesting substructures of your data:

```jinja2
{# find all private fields of the class #}
{% set private_fields = node | jsonpath("$.fields[?(@.visibility == 'private')]") %}

```

See the [JsonPath GitHub project](https://github.com/json-path/JsonPath) for a full documentation on how JsonPath works
and what expressions you can use.

It is also possible to inject data through system properties using the `sp` filter:
  
```jinja2
# Assuming you have set a system property with -DsomeProp=someValue
{{ 'someProp' | sp }}  # will print someValue

```

Finally a thing that is often required when generating code is case-changing of identifiers, so this package adds a
custom filter for this as well. The syntax of this filter is:

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

If you have specific needs for your project you can also write custom filters in JavaScript. A filter is simply a javascript 
function that receives objects and works on them. The functions will be executed inside the Rhino scripting engine
so you can use all of it's functionality (including full access to the Java API) in your scripts. 

```javascript
// a simple filter which multiplies the input
function times(input, interpreter, arguments) {
    return input * arguments[0];
}
```

The three arguments of the function are:
* `input` - the object to filter
* `interpreter` - an instance of `com.hubspot.jinjava.interpret.JinjavaInterpreter`.
* `arguments` - the arguments given to the filter in the template (an array of objects)

To register your custom filter, add the following to your `config.yml`:

```yaml
customFilters:
   # Path to the script file containing the filter function, relative to config.yml
 - script: filters/myFilter.js
   # Name of the filter function. This is also the name that will be used for the filter inside the template engine.
   function: times

  # more custom filters..

```
  
Then you can use your filter inside your templates like this:

```jinja2
{{ 5 | times(2) }} {# prints 10 #}
```
   
You can find a larger example in [simplegen-maven-example!](simplegen-maven-example/)

