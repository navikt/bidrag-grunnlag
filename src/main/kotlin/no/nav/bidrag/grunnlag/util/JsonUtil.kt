package no.nav.bidrag.grunnlag.util

import com.fasterxml.jackson.databind.ObjectMapper

fun <T> toJsonString(entity: T): String = ObjectMapper().findAndRegisterModules().writeValueAsString(entity)
