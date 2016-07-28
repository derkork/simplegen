#SimpleGen -  a simple code generator

This is an attempt to write a minimalistic yet powerful general-purpose code generator.


## Usage

```
java -jar simplegen.jar
```


## Introduction

The generator has no parameters. All configuration is in a file named ``config.yml``, which must reside in the current
working directory. The generator works by reading data from YAML files and then applying Jinja2 templates on the
read data. 


