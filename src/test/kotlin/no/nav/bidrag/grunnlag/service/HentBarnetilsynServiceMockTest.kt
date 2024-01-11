package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.familieefsak.FamilieEfSakConsumer
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

@ExtendWith(MockitoExtension::class)
class HentBarnetilsynServiceMockTest {

    @InjectMocks
    private lateinit var hentBarnetilsynService: HentBarnetilsynService

    @Mock
    private lateinit var barnetilsynConsumerMock: FamilieEfSakConsumer

    @Test
    fun `Skal returnere grunnlag når consumer-response er SUCCESS`() {
        Mockito.`when`(barnetilsynConsumerMock.hentBarnetilsyn(any())).thenReturn(RestResponse.Success(TestUtil.byggBarnetilsynResponse()))

        val barnetilsynRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val barnetilsynListe = hentBarnetilsynService.hentBarnetilsyn(
            barnetilsynRequestListe = barnetilsynRequestListe,
        )

        Mockito.verify(barnetilsynConsumerMock, Mockito.times(1)).hentBarnetilsyn(any())

        assertAll(
            { assertThat(barnetilsynListe).isNotNull() },
            { assertThat(barnetilsynListe).hasSize(2) },
            { assertThat(barnetilsynListe[0].barnPersonId).isEqualTo("01012212345") },
            { assertThat(barnetilsynListe[1].barnPersonId).isEqualTo("01011034543") },
        )
    }

    @Test
    fun `Skal returnere tomt grunnlag fra barnetilsyn når consumer-response er FAILURE`() {
        Mockito.`when`(barnetilsynConsumerMock.hentBarnetilsyn(any()))
            .thenReturn(RestResponse.Failure("Feilmelding", HttpStatus.NOT_FOUND, HttpClientErrorException(HttpStatus.NOT_FOUND)))

        val barnetilsynRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val barnetilsynListe = hentBarnetilsynService.hentBarnetilsyn(
            barnetilsynRequestListe = barnetilsynRequestListe,
        )

        Mockito.verify(barnetilsynConsumerMock, Mockito.times(1)).hentBarnetilsyn(any())

        assertAll(
            { assertThat(barnetilsynListe).isNotNull() },
            { assertThat(barnetilsynListe).isEmpty() },
        )
    }
}
