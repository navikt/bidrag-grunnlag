package no.nav.bidrag.grunnlag.util

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.bidrag.domene.util.trimToNull

fun <T> toJsonString(entity: T): String {
    return ObjectMapper().findAndRegisterModules().writeValueAsString(entity)
}