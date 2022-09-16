package com.ancientlightstudios.simplegen.filters

import com.hubspot.jinjava.interpret.JinjavaInterpreter
import com.hubspot.jinjava.lib.filter.Filter
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.PolyglotException
import org.graalvm.polyglot.proxy.ProxyExecutable
import java.util.logging.Handler
import java.util.logging.LogRecord
import javax.script.ScriptException

/**
 * A filter that executes a JavaScript.
 */
class ScriptFilter(val source: String, script: String, private val function: String) : Filter {

    // slf4j logger
    private val logger = org.slf4j.LoggerFactory.getLogger(javaClass)

    private val context:Context = Context.newBuilder("js")
            .allowExperimentalOptions(true)
            .allowAllAccess(true)

        .logHandler(object:Handler() {
            override fun publish(record: LogRecord) {
                logger.info(record.message)
            }

            override fun flush() {
                // nothing to do
            }

            override fun close() {
                // nothing to do
            }

        })
        .option("engine.WarnInterpreterOnly", "false")
            .option("js.syntax-extensions", "true")
            .option("js.nashorn-compat", "true").build()

    init {
        context.eval("js", script)
    }

    override fun getName(): String {
        return function
    }

    override fun filter(obj: Any?, interpreter: JinjavaInterpreter?, vararg args: String?): Any? {
        try {

            return context.getBindings("js").getMember(function).execute(obj, ProxyExecutable { arguments ->
                interpreter?.resolveELExpression(arguments[0].asString(), interpreter.lineNumber)
            }, args).`as`(Object::class.java)
        }
        catch(e: PolyglotException) {
            throw ScriptException(e.message, source, e.sourceLocation?.startLine ?: -1, e.sourceLocation?.startColumn ?: -1)
        }
        catch(e: ScriptException) {
            if (e.cause is PolyglotException) {
                val polyglotException = e.cause as PolyglotException
                throw ScriptException(e.message, source, polyglotException.sourceLocation?.startLine ?: -1, polyglotException.sourceLocation?.startColumn ?: -1)
            }
            throw e
        }
    }
}
