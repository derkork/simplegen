package com.ancientlightstudios.simplegen

import java.io.InputStream
import java.util.*

import javax.xml.stream.XMLEventReader

import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.Attribute


class XmlParser : DataParser {
    override val supportedDataFormats: Set<String> = setOf("application/xml", "text/xml")
    override fun init(configuration: Map<String, Any>) {
        // nothing to do
    }

    override fun parse(stream: InputStream, origin: String, configuration: Map<String, Any>): Map<String, Any> {
        val xmlInputFactory = XMLInputFactory.newInstance()
        val reader: XMLEventReader = xmlInputFactory.createXMLEventReader(stream)
        val stack = Stack<MutableMap<String,Any>>()

        var root:Map<String,Any>? = null

        while(reader.hasNext()) {
            val event = reader.nextEvent()
            when {
                event.isStartElement -> {
                    val startElement = event.asStartElement()
                    val newElement = mutableMapOf<String, Any>()
                    val newElementData = mutableMapOf<String,Any>()
                    if (root == null) {
                        root = newElement
                    }
                    newElement[startElement.name.localPart] = newElementData

                    startElement.attributes.forEach { val attr = it as Attribute
                        newElementData[attr.name.localPart] = attr.value
                    }

                    if (!stack.empty()) {
                        val parent = stack.peek()
                        @Suppress("UNCHECKED_CAST")
                        if (parent != null) {
                            val childList =
                                parent.computeIfAbsent(">") { mutableListOf<MutableMap<String, Any>>() } as MutableList<MutableMap<String, Any>>
                            childList.add(newElement)
                        }
                    }
                    stack.push(newElementData)
                }

                event.isCharacters -> {
                    val characters = event.asCharacters()
                    val parent = stack.peek()
                    if (parent != null) {
                        if (parent.containsKey("@text")) {
                            parent["@text"] = (parent["@text"] as String) + characters.data
                        }
                        else {
                            parent["@text"] = characters.data
                        }
                    }
                }

                event.isEndElement -> {
                    stack.pop()
                }
            }
        }

        if (root != null) {
            return root
        }
        return mapOf()
    }
}