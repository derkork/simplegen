# SimpleGen API package

This package provides an API for extending SimpleGen with your own modules. The API resides in a separate package to avoid adding a ton of unnecessary dependencies to projects that want to extend SimpleGen. To extend SimpleGen, create a new project (or reuse an existing one) and add a dependency to the `simplegen-api` package. Then you can implement the interfaces from this package to provide extensions.

## How to?
### Write a parser for a new data format?

If you want to use a custom data format as input for SimpleGen you can write a custom parser by implementing the `DataParser` interface. 

```kotlin
package com.example

class MyCustomParser : DataParser {
    /**
     * Return a list of mime types that your parser supports.
     */
    override val supportedDataFormats: Set<String> = setOf("application/my-custom-app")

    override fun parse(stream: InputStream, origin: String): Map<String, Any> {
        // here the actual parsing takes place
        // you get an InputStream with the data
        // and an origin string that tells you
        // from where the data came. The latter
        // is only intended for use in error messages.
        
        // parse the stream into a map structure.
        val result = ... 
        
        // and return it
        return result
    }
}
```

To make SimpleGen use your parser, use the JDKs Service Provider Interface (SPI). To do so, create a file `META-INF/services/com.ancientlightstudios.simplegen.DataParser`. Put a line with the fully qualified class name of your parser class into this file:

```
com.example.MyCustomParser
```

Then package this as a JAR file and put it into the classpath of SimpleGen when it is running. SimpleGen will automatically pick it up. 

See the [TOML](../simplegen-dataformat-toml) or [XML](../simplegen-dataformat-xml) parsers to get an example of a fully working project.