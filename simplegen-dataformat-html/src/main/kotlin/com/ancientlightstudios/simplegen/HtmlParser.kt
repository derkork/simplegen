package com.ancientlightstudios.simplegen

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.InputStream


class HtmlParser : DataParser {
    override val supportedDataFormats: Set<String> = setOf("text/html")
    lateinit var settings: Map<String, Any>

    override fun init(configuration: Map<String, Any>) {
        // csv settings are map below the "csv" key in the configuration
        settings = configuration["html"] as? Map<String, Any> ?: mapOf()
    }

    override fun parse(stream: InputStream, origin: String, configuration: Map<String, Any>): Map<String, Any> {

        val mergedSettings = settings + configuration

        val extractNestedText = booleanOrDefault(mergedSettings["extractNestedText"], false)
        val document = Jsoup.parse(stream, "UTF-8", origin)
        val result = mutableMapOf<String, Any>()
        val root = document.root().firstElementChild() ?: return result
        convertElement(root, result, extractNestedText)
        return result
    }


    private fun convertElement(element: Element, result: MutableMap<String, Any>, extractNestedText: Boolean) {
        val name = element.tagName()
        val attributes = element.attributes()
        val children = element.children()

        val elementMap = mutableMapOf<String, Any>()
        result[name] = elementMap

        // put all element attributes into the element map
        for (attribute in attributes) {
            elementMap[attribute.key] = attribute.value
        }

        // put the text into the element map under the @text key
        elementMap["@text"] = element.ownText()
        if (extractNestedText) {
            elementMap["@nestedText"] = element.text()
        }

        // put all children into the element map as an array under the '>' key
        // and recursively convert them
        if (children.isNotEmpty()) {
            val childrenArray = mutableListOf<Any>()
            elementMap[">"] = childrenArray
            for (child in children) {
                val childMap = mutableMapOf<String, Any>()
                childrenArray.add(childMap)
                convertElement(child, childMap, extractNestedText)
            }
        }
    }
}