package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.pensjon.PensjonConsumer
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
class HentBarnetilleggServiceMockTest {

    @InjectMocks
    private lateinit var hentBarnetilleggService: HentBarnetilleggService

    @Mock
    private lateinit var pensjonConsumerMock: PensjonConsumer

    @Test
    fun `Skal returnere grunnlag når consumer-response er SUCCESS`() {
        Mockito.`when`(pensjonConsumerMock.hentBarnetilleggPensjon(any()))
            .thenReturn(RestResponse.Success(TestUtil.byggHentBarnetilleggPensjonResponse()))

        val barnetilleggPensjonRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val barnetilleggPensjonListe = hentBarnetilleggService.hentBarnetilleggPensjon(
            barnetilleggPensjonRequestListe = barnetilleggPensjonRequestListe,
        )

        Mockito.verify(pensjonConsumerMock, Mockito.times(1)).hentBarnetilleggPensjon(any())

        assertAll(
            { assertThat(barnetilleggPensjonListe).isNotNull() },
            { assertThat(barnetilleggPensjonListe).hasSize(2) },
            { assertThat(barnetilleggPensjonListe[0].beløpBrutto).isEqualTo(BigDecimal.valueOf(1000.11)) },
            { assertThat(barnetilleggPensjonListe[1].beløpBrutto).isEqualTo(BigDecimal.valueOf(2000.22)) },
        )
    }

    @Test
    fun `Skal returnere tomt grunnlag fra barnetillegg når consumer-response er FAILURE`() {
        Mockito.`when`(pensjonConsumerMock.hentBarnetilleggPensjon(any()))
            .thenReturn(RestResponse.Failure("Feilmelding", HttpStatus.NOT_FOUND, HttpClientErrorException(HttpStatus.NOT_FOUND)))

        val barnetilleggPensjonRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val barnetilleggPensjonListe = hentBarnetilleggService.hentBarnetilleggPensjon(
            barnetilleggPensjonRequestListe = barnetilleggPensjonRequestListe,
        )

        Mockito.verify(pensjonConsumerMock, Mockito.times(1)).hentBarnetilleggPensjon(any())

        assertAll(
            { assertThat(barnetilleggPensjonListe).isNotNull() },
            { assertThat(barnetilleggPensjonListe).isEmpty() },
        )
    }
}
