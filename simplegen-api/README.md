# SimpleGen API package

This package provides an API for extending SimpleGen with your own modules. The API resides in a separate package to avoid adding a ton of unnecessary dependencies to projects that want to extend SimpleGen. To extend SimpleGen, create a new project (or reuse an existing one) and add a dependency to the `simplegen-api` package. Then you can implement the interfaces from this package to provide extensions.

## How to?
### Write a parser for a new data format?

If you want to use a custom data format as input for SimpleGen you can write a custom parser by implementing the `DataParser` interface. 

```kotlin

```