# SimpleGen - TOML data format support

This package provides support for using TOML as data files. TOML will be parsed into a map-like tree structure, according to TOMLs specification and design, e.g. the following TOML snippet:

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
