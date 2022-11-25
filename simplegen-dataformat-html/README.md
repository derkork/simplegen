# SimpleGen - HTML data format support

This package provides support for using HTML as data files. Since HTML has more possibilities for formatting data, the parsing of HTML into a tree-like map structure is not exactly straightforward. 

HTML has elements, attributes and text nodes which can be nested with each other. This structure has no direct match in a tree-like map structure. This parser tries to parse HTML in a reproducible way that is easy to interact with from the template engine while keeping the semantics of HTML intact.

## Usage
To read data in HTML format, simply add a `mimeType` indicator to your data section:

```yaml
transformations:
  - data:
      - includes: data.html
        mimeType: text/html
    # add template, nodes and outputPath configuration as usual
```

You can also mix and match data of different types:

```yaml
transformations:
  - data:
      - includes: data.html
        mimeType: application/html
      - other_data.yaml
    # add template, nodes and outputPath configuration as usual
```

## How HTML is converted during parsing
The following HTML:

```html
<!DOCTYPE html>
<html>
<head>
    <title>Example</title>
</head>
<body>
<p class="narf">This is an example of a simple HTML page with one paragraph.</p>
<div>Lorem ipsum <span>dolor sit</span> amet.</div>
</body>
</html>
```
would be parsed into this YAML equivalent:

```yaml
html:
  '>':
    - head:
        '>':
          - title: 
              '@text': Example
    - body:
        '>':
          - p:
              'class': narf
              '@text': This is an example of a simple HTML page with one paragraph.
          - div:
              '@text': Lorem ipsum amet.
              '@nestedText': Lorem ipsum dolor sit amet.

```
So each element will be parsed into an object which has the element name as a key and the element data as a value. The element data consists of the attributes, which are simply reproduced as key-value pairs. If the element has child elements, the element data will contain a special key named `>` which holds a list of all child elements in the order in which they appear in the HTML file. If an element has text, the element data will contain a special key named `@text` with the element's text as a value. Note that all text will be concatenated together so:

```xml
<span>
    this
    <span></span>
    will
    <span></span>
    be
    <span></span>
    merged
</span>
```

will yield:

```yaml
span:
  '@text': thiswillbemerged
  '>':
    - span: {}
    - span: {}
    - span: {}
```

All white space around text nodes will be discarded. HTML usually contains text interspersed with formatting tags and it may be useful to get the full text without having to walk the whhole tree. For this purpose, the parser will also add a special key named `@nestedText` to each element which contains the full text of the element including all nested elements. For example:

```html
<div>Lorem ipsum <span>dolor sit</span> amet.
```

will yield:

```yaml
div:
  '@text': Lorem ipsum amet.
  '@nestedText': Lorem ipsum dolor sit amet.
  '>':
    - span:
        '@text': dolor sit
        '@nestedText': dolor sit
```
    
## Accessing the data with the `jsonpath` filter

You can easily access the data in your templates with the built-in `jsonpath` filter. E.g. if you have a list of items in your HTML like this:

```html
<ul>
    <li class="item1">Yes</li>
    <li class="item2">No</li>
    <li class="item3">Maybe</li>
</ul>
```
and you want to render this into let's say a HTML `select` tag you could do it like this:

```html
<select>
    {# access the '>' list below the items and get all items from it #}
    {% set items = data | jsonpath('$.ul.>[*].li') %}
    {% for item in items %}
    <option value="{{ item.class }}">{{ item['@text'] }}</option>
    {% endfor %}
</select>
```

## Configuration

The HTML parsing can be configured globally or per each data fileset in `config.yaml`:

```yaml
extensions:
  # global configuration goes here
  html:
    extractNestedText: true

transformations:
    - data:
        - includes: data.html
          mimeType: text/html
          parserSettings:
            # per data fileset configuration goes here
            extractNestedText: false
```

The following configuration options are available:

| Option              | Description                                                                                                                                                              | Default |
|---------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| `extractNestedText` | Whether nested text should be extracted from the HTML elements. This can potentially consume a lot of memory, so you may want to disable this for very large HTML files. | `true`  |
