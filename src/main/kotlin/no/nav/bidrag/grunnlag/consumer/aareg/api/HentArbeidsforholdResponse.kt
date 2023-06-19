package no.nav.bidrag.grunnlag.consumer.aareg.api

data class HentArbeidsforholdResponse(
    val arbeidsforholdListe: Array<Arbeidsforhold>?
)

data class Arbeidsforhold(
    val ansettelsesdetaljer: List<Ansettelsesdetaljer>?,
    val ansettelsesperiode: Ansettelsesperiode?,
    val arbeidssted: Arbeidssted?,
    val arbeidstaker: Arbeidstaker?,
    val bruksperiode: Bruksperiode?,
    val navArbeidsforholdId: Int?,
    val navUuid: String?,
    val navVersjon: Int?,
    val opplysningspliktig: Opplysningspliktig?,
    val opprettet: String?,
    val rapporteringsordning: Rapporteringsordning?,
    val sistBekreftet: String?,
    val sistEndret: String?,
    val type: Type?
)

data class Ansettelsesdetaljer(
    val antallTimerPrUke: Double?,
    val arbeidstidsordning: Arbeidstidsordning?,
    val avtaltStillingsprosent: Double?,
    val rapporteringsmaaneder: Rapporteringsmaaneder?,
    val type: String?,
    val yrke: Yrke?
)

data class Ansettelsesperiode(
    val startdato: String?
)

data class Arbeidssted(
    val identer: List<Identer>,
    val type: String?
)

data class Arbeidstaker(
    val identer: List<ArbeidsgiverIdenter>
)

data class Bruksperiode(
    val fom: String?,
    val tom: String?
)

data class Opplysningspliktig(
    val identer: List<Identer>?,
    val type: String?
)

data class Rapporteringsordning(
    val beskrivelse: String?,
    val kode: String?
)

data class Type(
    val beskrivelse: String?,
    val kode: String?
)

data class Arbeidstidsordning(
    val beskrivelse: String?,
    val kode: String?
)

data class Rapporteringsmaaneder(
    val fra: String?,
    val til: String?
)

data class Yrke(
    val beskrivelse: String?,
    val kode: String?
)

data class Identer(
    val ident: String?,
    val type: String?
)

data class ArbeidsgiverIdenter(
    val gjeldende: Boolean?,
    val ident: String?,
    val type: String?
)
