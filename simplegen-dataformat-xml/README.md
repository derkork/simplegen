# SimpleGen - XML data format support

This package provides support for using XML as data files. Since XML has more possibilities for formatting data, the parsing of XML into a tree-like map structure is not exactly straightforward. 

XML has elements, attributes and text nodes which can be nested with each other. This structure has no direct match in a tree-like map structure. This parser tries to parse XML in a reproducible way that is easy to interact with from the template engine while keeping the semantics of XML intact.

## Parsing
The following XML:

```xml
<element attribute1="value1">
    <nested-element attribute2="value2"/>
    <element-with-text>text</element-with-text>
</element>
```
would be parsed into this YAML equivalent:

```yaml
element:
  attribute1: value1
  '>':
    - nested-element:
        attribute2: value2
    - element-with-text:
        '@text':text
```
So each element will be parsed into an object which has the element name as a key and the element data as a value. The element data consists of the attributes, which are simply reproduced as key-value pairs. If the element has child elements, the element data will contain a special key named `>` which holds a list of all child elements in the order in which they appear in the XML file. If an element has text, the element data will contain a special key named `@text` with the element's text as a value. Note that all text will be concatenated together so:

```xml
<foo>
    this
    <bar></bar>
    will
    <baz></baz>
    be
    <bam></bam>
    merged
</foo>
```

will yield:

```yaml
foo:
  '@text': thiswillbemerged
  '>':
    - bar: {}
    - baz: {}
    - bam: {}
```

All white space around text nodes will be discarded.

## Accessing the data with the `jsonpath` filter

You can easily access the data in your templates with the built-in `jsonpath` filter. E.g. if you have a list of items in your XML like this:

```xml
<items>
    <item name="item1">Yes</item>
    <item name="item2">No</item>
    <item name="item3">Maybe</item>
</items>
```
and you want to render this into let's say a HTML `select` tag you could do it like this:

```html
<select>
    {# access the '>' list below the items and get all items from it #}
    {% set items = data | jsonpath('$.items.>[*].item') %}
    {% for item in items %}
    <option value="{{ item.name }}">{{ item['@text'] }}</option>
    {% endfor %}
</select>
```