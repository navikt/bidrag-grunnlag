package no.nav.bidrag.grunnlag.api.deserialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode

// Hindrer default deserialisering hvor null blir til 0. Her tvinger vi frem en exception dersom feltet er null og ikke er nullable.
class IntDeserializer : JsonDeserializer<Int>() {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Int? {
        val node: JsonNode = p!!.codec.readTree(p)
        if (!node.isNull && node.isInt) {
            return node.asInt()
        }
        return null
    }
}
