package com.ancientlightstudios.simplegen.filters

import com.hubspot.jinjava.interpret.JinjavaInterpreter
import com.hubspot.jinjava.lib.filter.Filter

class FlattenFilter : Filter {

    override fun getName(): String = "flatten"
    override fun filter(obj: Any?, interpreter: JinjavaInterpreter?, vararg args: String?): Any? {
        // if the object is iterable, flatten it
        if (obj is Iterable<*>) {
            return obj.flatMap {
                when (it) {
                    is Iterable<*> -> it
                    is Array<*> -> it.asIterable()
                    else -> listOf(it)
                }
            }
        }

        // if it is an array, convert to an iterable and flatten that
        if (obj is Array<*>) {
            return filter(obj.asIterable(), interpreter, *args)
        }

        // otherwise just return it
        return obj
    }
}