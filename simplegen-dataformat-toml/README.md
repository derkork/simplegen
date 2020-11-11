# SimpleGen - TOML data format support

This package provides support for using TOML as data files. TOML will be parsed into a map-like tree structure, according to TOMLs specification and design.

## Usage
To read data in TOML format, simply add a `mimeType` indicator to your data section:

```yaml
transformations:
  - data:
      - includes: data.toml
        mimeType: application/toml
    # add template, nodes and outputPath configuration as usual
```

You can also mix and match data of different types:

```yaml
transformations:
  - data:
      - includes: data.toml
        mimeType: application/toml
      - other_data.yaml
    # add template, nodes and outputPath configuration as usual
```
## How TOML is converted during parsing
The following TOML snippet:

```toml
[example]
value1 = 5
value2 = "foo"

[example.subtree]
value3 = "bar"
value4 = "baz"
```

will be equivalent to the following YAML structure:

```yaml
example:
  value1: 5
  value2: "foo"
  subtree:
    value3: "bar"
    value4: "baz"
```
