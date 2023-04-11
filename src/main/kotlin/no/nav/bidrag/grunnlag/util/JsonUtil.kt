package no.nav.bidrag.grunnlag.util

import com.fasterxml.jackson.databind.ObjectMapper

fun<T> toJsonString(entity: T): String {
    return ObjectMapper().findAndRegisterModules().writeValueAsString(entity)
}
