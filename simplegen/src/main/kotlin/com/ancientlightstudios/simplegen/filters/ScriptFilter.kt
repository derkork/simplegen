package com.ancientlightstudios.simplegen.filters

import com.hubspot.jinjava.interpret.JinjavaInterpreter
import com.hubspot.jinjava.lib.filter.Filter
import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

/**
 * A filter that executes a JavaScript.
 */
class ScriptFilter(script: String, private val function: String) : Filter {

    private val engine:ScriptEngine = ScriptEngineManager().getEngineByName("nashorn")

    init {
        engine.eval(script)
    }

    override fun getName(): String {
        return function.toLowerCase()
    }

    override fun filter(obj: Any?, interpreter: JinjavaInterpreter?, vararg args: String?): Any? {
        return (engine as Invocable).invokeFunction(function, obj, interpreter, args)
    }
}