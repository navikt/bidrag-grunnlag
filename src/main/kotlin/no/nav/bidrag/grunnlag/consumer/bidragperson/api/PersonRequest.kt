package no.nav.bidrag.grunnlag.consumer.bidragperson.api

data class PersonRequest(
    val ident: String,
    val verdi: String = ident
)
