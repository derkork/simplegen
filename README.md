# SimpleGen -  A simple yet powerful general-purpose code generator

[![Build Status](https://travis-ci.org/derkork/simplegen.svg?branch=master)](https://travis-ci.org/derkork/simplegen)

This is an attempt to write a simple yet powerful general-purpose code generator. It is not specialized to any language,
so whatever your target language is (may it be Java, Kotlin, C#, Ruby, JavaScript, HTML etc.), if it is text then 
this generator should be able to create it.

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

``` 
    <build>
        <plugins>
            ...
            <plugin>
                <groupId>com.ancientlightstudios</groupId>
                <artifactId>simplegen-maven-plugin</artifactId>
                <version>1.0.3</version>
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

### Configuration

The generator will read instructions from the configuration file and execute them in the order specified there. Instructions
look like this:

```
transformations:
  
    # From where to pull the data. You can actually pull more than one, in which case their contents get merged.
    # Paths are relative to the config.yml file.
  - data: 
      - data.yml
      # Also ant style file selection is supported
      - **/*.yml
      # or even full-blown
      - includes: **/.yml
        excludes: **/foo.yml
        basePath: ../some/other/folder
      
  	# Which template should be usd to render the data. Specify the path relative to the config.yml file.  
    template: template.j2
    # For which nodes in the data should the template be executed. This is a JsonPath.
    nodes: myclasses
    # What should be the output path of the file.
    outputPath: "{{ node.package | replace('.', '/') }}/{{ node.name }}.java"

```

Starting with version 1.0.3 you can use expressions in all fields of the configuration. So e.g. if you want to pull
data from a folder set by a system property you can do it like this:

```
# Assuming you have set the system property 'input.path' to '/some/path'

transformations:
  - data: 
      - includes: **/.yml
        basePath: "{{ 'input.path' | sp }}"  # will set the basePath to /some/path
```

Starting with version 1.0.4 you can now also configure the template engine within `config.yml`. The template engine
configuration is optional, if you leave it out, all values will be initialized with `false` (as it was the default
for the previous version of simplegen where this configuration option didn't exist). You can have a global configuration 
for all transformations which you can override per transformation like this:

```
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



### Data

Now you need a data file which gives the generator a data model to work on. In this example let's create
empty java classes from a model. The data file for this could look like this:

```
myclasses:
  - package: com.ancientlightstudios.myapp
    name: SomeClass
    fields:
    	- name: someField
    	- visibility: private
    	- type: java.lang.String
    	
  - package: com.ancientlightstudios.myapp
    name: SomeOtherClass
    fields:
    	- name: someField
    	- visibility: public
    	- type: boolean
```

The data file is totally free-form, you can structure it any way that fits your needs. There are no special keywords
that the generator acts upon, it's just a structured data file. Now with the configuration above, what happens is that
the generator will execute the template ``template.j2`` for each node below the ``myclasses`` node.  The result of this
template will be stored in a ``.java`` file corresponding to the node's ``name`` and ``package`` properties. 

### Templates
  
The last missing thing is the template and this looks like this:

```
package {{ node.package }};

class {{ node.name }} {
}

```

It just creates an empty class which is not particulary useful but it should be sufficient to get the idea across. The
template is a Jinja2 template and in it you have access to two variables:

* ``node`` - this is the part of the data that is currently being processed. In this example it would be the current node
  below the ``myclasses`` parent.
* ``data`` - this contains the whole data from all scanned data files. This can be useful if you have global information
  that you want to share across templates.
  
You can use every feature of the Jinja2 language including macros, includes, etc. When you include things remember that
all paths must be specified relative to the ``config.yml`` file.  

#### Additional built-in filters

In addition to the standard filters, this package adds a `jsonpath` filter to the template engine, so you can use 
JSONPath to effectively select interesting substructures of your data:

```
{% set private_fields = node | jsonpath("$.fields[?(@.visibility == 'private')]") %}

```

It is also possible to inject data through system properties using the `sp` filter:
  
```
# Assuming you have set a system property with -DsomeProp=someValue
{{ 'someProp' | sp }}  # will print someValue

```

Finally a thing that is often required when generating code is case-changing of identifiers, so this package adds a
custom filter for this as well. The syntax of this filter is:

```
	case( <input case>, <output case> )
```

Supported case formats are:

* 'upper-camel' - FooBar
* 'lower-camel' - fooBar
* 'lower-hyphen' - foo-bar
* 'upper-underscore' - FOO_BAR
* 'lower-underscore' - foo_bar

```
{{ 'SomeString' | case('upper-camel', 'lower-hyphen') }} # will print 'some-string'
{{ 'someString' | case('lower-camel', 'upper-camel') }} # will print 'SomeString'
{{ 'some-string' | case('lower-hyphen', 'upper-camel') }} # will print 'SomeString'

```
  
You can find a larger example in [simplegen-maven-example!](simplegen-maven-example/)

