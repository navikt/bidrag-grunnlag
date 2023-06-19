package no.nav.bidrag.grunnlag.consumer.aareg.api

data class HentArbeidsforholdRequest(
    val navPersonident: String,
    val arbeidsforholdtypeFilter: String? = null,
    val rapporteringsordningFilter: String? = null,
    val arbeidsforholdstatusFilter: String? = null,
    val historikk: Boolean = true,
    val sporingsinformasjon: Boolean = true
)
