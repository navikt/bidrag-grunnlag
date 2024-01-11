package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.api.BisysStønadstype
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.any
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class HentUbstServiceMockTest {

    @InjectMocks
    private lateinit var hentUbstService: HentUtvidetBarnetrygdOgSmåbarnstilleggService

    @Mock
    private lateinit var ubstConsumerMock: FamilieBaSakConsumer

    @Test
    fun `Skal returnere grunnlag når consumer-response er SUCCESS`() {
        Mockito.`when`(ubstConsumerMock.hentFamilieBaSak(any())).thenReturn(RestResponse.Success(TestUtil.byggFamilieBaSakResponse()))

        val ubstRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val ubstListe = hentUbstService.hentUbst(
            ubstRequestListe = ubstRequestListe,
        )

        Mockito.verify(ubstConsumerMock, Mockito.times(1)).hentFamilieBaSak(any())

        assertAll(
            { assertThat(ubstListe).isNotNull() },
            { assertThat(ubstListe).hasSize(2) },
            { assertThat(ubstListe[0].type).isEqualTo(BisysStønadstype.UTVIDET.toString()) },
            { assertThat(ubstListe[0].beløp).isEqualTo(BigDecimal.valueOf(1000.11)) },
            { assertThat(ubstListe[1].type).isEqualTo(BisysStønadstype.UTVIDET.toString()) },
            { assertThat(ubstListe[1].beløp).isEqualTo(BigDecimal.valueOf(2000.22)) },
        )
    }

    @Test
    fun `Skal returnere tomt grunnlag fra utvidet barnetrygd og småbarnstillegg når consumer-response er FAILURE`() {
        Mockito.`when`(ubstConsumerMock.hentFamilieBaSak(any()))
            .thenReturn(RestResponse.Failure("Feilmelding", HttpStatus.NOT_FOUND, HttpClientErrorException(HttpStatus.NOT_FOUND)))

        val ubstRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val ubstListe = hentUbstService.hentUbst(
            ubstRequestListe = ubstRequestListe,
        )

        Mockito.verify(ubstConsumerMock, Mockito.times(1)).hentFamilieBaSak(any())

        assertAll(
            { assertThat(ubstListe).isNotNull() },
            { assertThat(ubstListe).isEmpty() },
        )
    }
}
