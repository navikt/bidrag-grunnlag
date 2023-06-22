package no.nav.bidrag.grunnlag.consumer.aareg.api

// data class HentArbeidsforholdResponse(
//    val arbeidsforholdListe: List<Arbeidsforhold>?
// )

data class Arbeidsforhold(
    val ansettelsesdetaljer: List<Ansettelsesdetaljer>? = emptyList(),
    val ansettelsesperiode: Ansettelsesperiode? = null,
    val arbeidssted: Arbeidssted? = null,
    val arbeidstaker: Arbeidstaker? = null,
    val bruksperiode: Bruksperiode? = null,
    val navArbeidsforholdId: Int? = null,
    val id: String? = null,
    val navVersjon: Int? = null,
    val opplysningspliktig: Opplysningspliktig? = null,
    val opprettet: String? = null,
    val rapporteringsordning: Rapporteringsordning? = null,
    val sistBekreftet: String? = null,
    val sistEndret: String? = null,
    val type: Type? = null
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
