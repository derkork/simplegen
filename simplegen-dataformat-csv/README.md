# SimpleGen - CSV data format support

This package provides support for using CSV as data files. CSV will be parsed into a list of maps, where each map represents a row in the CSV file. The keys of the map are the column names, and the values are the values in the row.

## Usage
To read data in CSV format, simply add a `mimeType` indicator to your data section:

```yaml
transformations:
  - data:
      - includes: data.csv
        mimeType: text/csv
    # add template, nodes and outputPath configuration as usual
```

You can also mix and match data of different types:

```yaml
transformations:
  - data:
      - includes: data.csv
        mimeType: application/csv
      - other_data.yaml
    # add template, nodes and outputPath configuration as usual
```
## How CSV is converted during parsing
The following CSV snippet:

```csv
id,name,age
1,John,30
2,Paul,40
```


will be equivalent to the following YAML structure:

```yaml
csv:
  - id: 1
    name: John
    age: 30
  - id: 2
    name: Paul
    age: 40
```

## Configuration

The CSV parsing can be configured globally or per each data fileset in `config.yaml`:

```yaml
extensions:
  # global configuration goes here
  csv:
    separatorChar: ";"
    quoteChar: '"'

transformations:
    - data:
        - includes: data.csv
          mimeType: application/csv
          parserSettings:
            # per data fileset configuration goes here
            separatorChar: ","
            quoteChar: "'"
```

The following configuration options are available:

| Option          | Description                                                                      | Default |
|-----------------|----------------------------------------------------------------------------------|---------|
| `separatorChar` | The character used to separate values in the CSV file.                           | `,`     |
| `quoteChar`     | The character used to quote values in the CSV file.                              | `"`     |
| `escapeChar`    | The character used to escape special characters in the CSV file.                 | `\\`    |
| `skipLines`     | The number of lines to skip before parsing the CSV file.                         | `0`     |
| `charset`       | The character set used to read the CSV file.                                     | `UTF-8` |
| `resultPath`    | The path to the result in the data model. Nested paths are separated with a `.`. | `csv`   |    
| `stripBom`      | Whether to strip an UTF-8 byte order mark from the input if it exists.           | `true`  |




