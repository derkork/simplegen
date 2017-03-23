package com.ancientlightstudios.simplegen

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

object JsonUtil {
    /**
     * Merges all the given json nodes into one. The first node will be the result.
     * @param maps the nodes to merge
     * @return the first given node
     */
    fun merge(vararg maps: Map<String, Any>): Map<String, Any> {
        val mapper = ObjectMapper()

        if (maps.size == 1) {
            return maps[0]
        }

        if (maps.isEmpty()) {
            throw IllegalArgumentException("Must give at least two nodes to merge.")
        }

        val nodes = maps.map { mapper.convertValue(it, JsonNode::class.java) }

        for (node in nodes.slice(1..nodes.size - 1)) {
            merge(nodes[0], node)
        }

        @Suppress("UNCHECKED_CAST")
        return mapper.treeToValue(nodes[0], Map::class.java) as Map<String,Any>
    }

    private fun merge(mainNode: JsonNode, updateNode: JsonNode) {

        val fieldNames = updateNode.fieldNames()

        while (fieldNames.hasNext()) {
            val updatedFieldName = fieldNames.next()
            val valueToBeUpdated = mainNode.get(updatedFieldName)
            val updatedValue = updateNode.get(updatedFieldName)

            // If the node is an @ArrayNode
            if (valueToBeUpdated != null && updatedValue.isArray) {
                // running a loop for all elements of the updated ArrayNode
                @Suppress("LoopToCallChain")
                for (i in 0..updatedValue.size() - 1) {
                    val updatedChildNode = updatedValue.get(i)
                    // Create a new Node in the node that should be updated, if there was no corresponding node in it
                    // Use-case - where the updateNode will have a new element in its Array
                    (valueToBeUpdated as ArrayNode).add(updatedChildNode)
                }
                // if the Node is an @ObjectNode
            } else if (valueToBeUpdated != null && valueToBeUpdated.isObject) {
                merge(valueToBeUpdated, updatedValue)
            } else {
                if (mainNode is ObjectNode) {
                    mainNode.replace(updatedFieldName, updatedValue)
                }
            }
        }
    }
}
