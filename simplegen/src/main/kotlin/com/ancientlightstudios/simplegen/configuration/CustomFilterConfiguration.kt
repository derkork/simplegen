package com.ancientlightstudios.simplegen.configuration

class CustomFilterConfiguration(val script: String = "", val function: Any = emptyList<Any>()) {
    val parsedFunctions:List<String> by lazy { function.asStringList() }
}