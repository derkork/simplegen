package com.ancientlightstudios.simplegen

import org.tomlj.TomlArray
import org.tomlj.TomlTable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime

/**
 * Custom JSON serializer that serializes TOML into JSON which we can feed into Jackson later.
 * Not using TOMLjs serializer because it is buggy, and I actually do not need indentation, so this is a
 * streamlined version that does what I need, and I have it under control.
 */
object JsonSerializer {
    fun toJson(table: TomlTable, appendable: Appendable) {
        toJson(table, appendable, 0)
        appendable.append(System.lineSeparator())
    }

    private fun toJson(table: TomlTable, appendable: Appendable, indent: Int) {
        if (table.isEmpty) {
            appendable.append("{}")
            return
        }
        appendLine(appendable)
        val keys = table.keySet()
        for ((index, key) in keys.withIndex()) {
            append(appendable, "\"" + escape(key) + "\" : ")
            val value = table.get(key)
            appendTomlValue(value!!, appendable, indent)
            if (index + 1 < keys.size) {
                appendable.append(",")
                appendable.append(System.lineSeparator())
            }
        }
        appendable.append(System.lineSeparator())
        append(appendable, "}")
    }

    private fun toJson(array: TomlArray, appendable: Appendable, indent: Int) {
        if (array.isEmpty) {
            appendable.append("[]")
            return
        }
        appendable.append("[")
        val iterator: Iterator<Any> = array.toList().iterator()
        var lastValue: Any? = null
        while (iterator.hasNext()) {
            lastValue = iterator.next()
            if (lastValue is TomlTable) {
                toJson(lastValue, appendable, indent)
            } else {
                appendTomlValue(lastValue, appendable, indent)
            }
            if (iterator.hasNext()) {
                appendable.append(",")
            } else if (lastValue !is TomlTable) {
                appendable.append(System.lineSeparator())
            }
        }
        if (lastValue is TomlTable) {
            appendable.append("]")
        } else {
            append(appendable, "]")
        }
    }

    private fun appendTomlValue(value: Any, appendable: Appendable, indent: Int) {
        when (value) {
            is String -> append(appendable, "\"" + escape(value) + "\"")
            is Number -> append(appendable, value.toString())
            is Boolean -> append(appendable, if (value) "true" else "false")
            is OffsetDateTime, is LocalDateTime, is LocalDate, is LocalTime -> append(appendable, "\"" + value.toString() + "\"")
            is TomlArray -> toJson(value, appendable, indent + 2)
            is TomlTable -> toJson(value, appendable, indent + 2)
        }
    }

    private fun append(appendable: Appendable, line: String) {
        appendable.append(line)
    }

    private fun appendLine(appendable: Appendable) {
        appendable.append("{")
        appendable.append(System.lineSeparator())
    }

    private fun escape(text: String): StringBuilder {
        val out = StringBuilder(text.length)
        for (i in text.indices) {
            val ch = text[i]
            if (ch == '"') {
                out.append("\\\"")
                continue
            }
            if (ch == '\\') {
                out.append("\\\\")
                continue
            }
            if (ch.code >= 0x20) {
                out.append(ch)
                continue
            }
            when (ch) {
                '\t' -> out.append("\\t")
                '\b' -> out.append("\\b")
                '\n' -> out.append("\\n")
                '\r' -> out.append("\\r")
                '\u000C' -> out.append("\\f")
                else -> out.append("\\u").append(String.format("%04x", text.codePointAt(i)))
            }
        }
        return out
    }
}
