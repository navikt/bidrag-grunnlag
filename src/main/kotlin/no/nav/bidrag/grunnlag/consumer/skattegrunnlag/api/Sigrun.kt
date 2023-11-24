package no.nav.bidrag.grunnlag.consumer.skattegrunnlag.api

data class HentSummertSkattegrunnlagRequest(
    val inntektsAar: String,
    val inntektsFilter: String,
    val personId: String,
)

data class HentSummertSkattegrunnlagResponse(
    val grunnlag: List<Skattegrunnlag>?,
    val svalbardGrunnlag: List<Skattegrunnlag>?,
    val skatteoppgjoersdato: String?,
)

data class Skattegrunnlag(
    val beloep: String,
    val tekniskNavn: String,
)
