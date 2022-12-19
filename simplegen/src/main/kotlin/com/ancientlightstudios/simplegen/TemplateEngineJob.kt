package com.ancientlightstudios.simplegen

class TemplateEngineJob(val source:String, val template:String ) {
    val context: MutableMap<String, Any> = mutableMapOf()

    fun with(context:Map<String, Any>):TemplateEngineJob {
        this.context.putAll(context)
        return this
    }

    fun with(data:Any, node:Any) = with(mapOf("data" to data, "node" to node))

}