#SimpleGen -  A simple yet powerful general-purpose code generator

This is an attempt to write a simple yet powerful general-purpose code generator. It is not specialized to any language,
so whatever your target language is (may it be Java, Kotlin, C#, Ruby, JavaScript, HTML etc.), if it is text then 
this generator should be able to create it.

## Usage

The generator can be used standalone or as a Maven plugin. In both cases it expects an input structure that looks like this:
 
* ``config.yml`` - the configuration file
* ``data.yml`` - a data file (you can choose any name you want, actually)
* ``template.j2`` - at least one template

The generator will read instructions from the configuration file and execute them in the order specified there. Instructions
look like this:

```
transformations:
  
    # From where to pull the data. You can actually pull more than one, in which case their contents get merged.
    # Paths are relative to the config.yml file.
  - data: 
      - data.yml
  	# Which template should be usd to render the data. Specify the path relative to the config.yml file.  
    template: template.j2
    # For which nodes in the data should the template be executed. This is a JsonPath.
    nodes: myclasses
    # What should be the output path of the file.
    outputPath: "{{ node.package | replace('.', '/') }}/{{ node.name }}.java"

```

Now you need a data file which gives the generator a data model to work on. In this example let's create
empty java classes from a model. The data file for this could look like this:

```
myclasses:
  - package: com.ancientlightstudios.myapp
    name: SomeClass
  - package: com.ancientlightstudios.myapp
    name: SomeOtherClass
```

The data file is totally free-form, you can structure it any way that fits your needs. There are no special keywords
that the generator acts upon, it's just a structured data file. Now with the configuration above, what happens is that
the generator will execute the template ``template.j2`` for each node below the ``myclasses`` node.  The result of this
template will be stored in a ``.java`` file corresponding to the node's ``name`` and ``package`` properties. 
  
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
  
You can find a larger example in [simplegen-maven-example!](simplegen-maven-example/)

