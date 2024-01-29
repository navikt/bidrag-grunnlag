package no.nav.bidrag.grunnlag.util

import no.nav.bidrag.domene.enums.grunnlag.GrunnlagRequestType
import no.nav.bidrag.domene.enums.vedtak.Formål
import org.mockito.Mockito

open class GrunnlagUtil {

    companion object {
        const val BIDRAG_FILTER = "BidragA-Inntekt"
        const val FORSKUDD_FILTER = "BidragsforskuddA-Inntekt"
        const val BIDRAG_FORMAAL = "Bidrag"
        const val FORSKUDD_FORMAAL = "Bidragsforskudd"
        const val JANUAR2015 = "2015-01"

        fun finnFilter(formaal: String): String {
            return if (formaal == Formål.FORSKUDD.toString()) FORSKUDD_FILTER else BIDRAG_FILTER
        }

        fun finnFormaal(formaal: String): String {
            return if (formaal == Formål.FORSKUDD.toString()) FORSKUDD_FORMAAL else BIDRAG_FORMAAL
        }

        fun evaluerFeilmelding(melding: String?, grunnlagstype: GrunnlagRequestType): String {
            if (melding == null) return ""

            return when (grunnlagstype) {
                GrunnlagRequestType.BARNETILSYN,
                GrunnlagRequestType.UTVIDET_BARNETRYGD_OG_SMÅBARNSTILLEGG,
                GrunnlagRequestType.KONTANTSTØTTE,
                -> {
                    val regex = Regex("\"melding\":\"(.*?)\"")
                    val matchResult = regex.find(melding)
                    matchResult?.groupValues?.get(1) ?: melding.take(100)
                }

                GrunnlagRequestType.SKATTEGRUNNLAG -> {
                    var regex = Regex("\"melding\":\"(.*?)\"")
                    var matchResult = regex.find(melding)
                    matchResult?.groupValues?.get(1) ?: run {
                        regex = Regex("\"message\":\"(.*?)\"")
                        matchResult = regex.find(melding)
                        matchResult?.groupValues?.get(1) ?: melding.take(100)
                    }
                }

                GrunnlagRequestType.AINNTEKT -> {
                    val regex = Regex("\"message\":\"(.*?)\"")
                    val matchResult = regex.find(melding)
                    matchResult?.groupValues?.get(1) ?: melding.take(100)
                }

                GrunnlagRequestType.ARBEIDSFORHOLD -> {
                    val regex = Regex("\"meldinger\":\\[\"(.*?)\"]")
                    val matchResult = regex.find(melding)
                    matchResult?.groupValues?.get(1) ?: melding.take(100)
                }

                GrunnlagRequestType.BARNETILLEGG -> {
                    var regex = Regex("\"feil\":\"(.*?)\"")
                    var matchResult = regex.find(melding)
                    matchResult?.groupValues?.get(1) ?: run {
                        regex = Regex("\"(.*?)\"")
                        matchResult = regex.find(melding)
                        matchResult?.groupValues?.get(1) ?: melding.take(100)
                    }
                }

                GrunnlagRequestType.SIVILSTAND,
                GrunnlagRequestType.HUSSTANDSMEDLEMMER_OG_EGNE_BARN,
                -> {
                    val regex = Regex("\"Warning\":\\[\"(.*?)\"]")
                    val matchResult = regex.find(melding)
                    matchResult?.groupValues?.get(1) ?: melding.take(100)
                }

                else -> melding
            }
        }

        fun <T> any(type: Class<T>): T = Mockito.any(type)

        fun <T> any(): T = Mockito.any()
    }
}
