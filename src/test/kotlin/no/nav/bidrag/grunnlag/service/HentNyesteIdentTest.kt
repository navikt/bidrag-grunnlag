package no.nav.bidrag.grunnlag.service

import io.micrometer.core.instrument.MeterRegistry
import no.nav.bidrag.commons.util.IdentConsumer
import no.nav.bidrag.grunnlag.TestUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class HentNyesteIdentTest {

    @InjectMocks
    private lateinit var grunnlagspakkeService: GrunnlagspakkeService

    @Mock
    private lateinit var persistenceServiceMock: PersistenceService

    @Mock
    private lateinit var meterRegistry: MeterRegistry

    @Mock
    private lateinit var oppdaterGrunnlagspakkeService: OppdaterGrunnlagspakkeService

    @Mock
    private lateinit var identConsumerMock: IdentConsumer

    @Test
    fun `sjekker at innsendt ident brukes hvis ingen historiske identer blir funnet`() {
        val request = TestUtil.byggOppdaterGrunnlagspakkeRequestKontantstotteIdentFraDolly()
        val forventetIdentReturnert = "13476240306"

        `when`(identConsumerMock.sjekkIdent("14508708972")).thenReturn("13476240306")

        // Kaller metoden som skal testes (privat metode vha reflection)
//        val modifisertRequest: OppdaterGrunnlagspakkeRequestDto =
//            invokePrivateMethod(grunnlagspakkeService, "hentAktivIdent", request) as OppdaterGrunnlagspakkeRequestDto
        val modifisertRequest = grunnlagspakkeService.hentAktivIdent(request)

        verify(identConsumerMock, times(1)).sjekkIdent("14508708972")

        // Sjekker at responsen er som forventet
        assertAll(
            { assertThat(modifisertRequest).isNotNull() },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe.size).isEqualTo(request.grunnlagRequestDtoListe.size) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].personId).isEqualTo(forventetIdentReturnert) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].personId).isNotEqualTo(request.grunnlagRequestDtoListe[0].personId) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].periodeFra).isEqualTo(request.grunnlagRequestDtoListe[0].periodeFra) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].periodeTil).isEqualTo(request.grunnlagRequestDtoListe[0].periodeTil) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].type).isEqualTo(request.grunnlagRequestDtoListe[0].type) },
        )
    }

    // Bruker reflection for å kalle private metoder
//    private fun invokePrivateMethod(klasse: Any, metode: String, vararg args: Any): Any? {
//        val method: Method = klasse.javaClass.getDeclaredMethod(metode, *args.map { it.javaClass }.toTypedArray())
//        method.isAccessible = true
//        return method.invoke(klasse, *args)
//    }

    object MockitoHelper {
        fun <T> any(type: Class<T>): T = Mockito.any(type)
        fun <T> any(): T = Mockito.any()
    }
}
