package no.nav.bidrag.grunnlag.util

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

        fun <T> any(type: Class<T>): T = Mockito.any(type)

        fun <T> any(): T = Mockito.any()
    }
}
