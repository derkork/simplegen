package com.ancientlightstudios.simplegen.configuration

fun Any?.asStringList(): List<String> {
    return when {
        this is String -> listOf(this)
        this is List<*> -> this.map { it.toString() }
        this != null -> listOf(this.toString())
        else -> emptyList()
    }
}
