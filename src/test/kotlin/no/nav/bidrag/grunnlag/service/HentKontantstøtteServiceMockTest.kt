package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.familiekssak.FamilieKsSakConsumer
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
class HentKontantstøtteServiceMockTest {

    @InjectMocks
    private lateinit var hentKontantstøtteService: HentKontantstøtteService

    @Mock
    private lateinit var kontantstøtteConsumerMock: FamilieKsSakConsumer

    @Test
    fun `Skal returnere grunnlag når consumer-response er SUCCESS`() {
        Mockito.`when`(kontantstøtteConsumerMock.hentKontantstotte(any())).thenReturn(RestResponse.Success(TestUtil.byggKontantstotteResponse()))

        val kontantstøtteRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val kontantstøtteListe = hentKontantstøtteService.hentKontantstøtte(
            kontantstøtteRequestListe = kontantstøtteRequestListe,
            emptyMap(),
        )

        Mockito.verify(kontantstøtteConsumerMock, Mockito.times(1)).hentKontantstotte(any())

        assertAll(
            { assertThat(kontantstøtteListe).isNotNull() },
            { assertThat(kontantstøtteListe).hasSize(3) },
            { assertThat(kontantstøtteListe[0].barnPersonId).isEqualTo("11223344551") },
            { assertThat(kontantstøtteListe[1].barnPersonId).isEqualTo("15544332211") },
            { assertThat(kontantstøtteListe[2].barnPersonId).isEqualTo("11223344551") },
        )
    }

    @Test
    fun `Skal returnere tomt grunnlag fra kontantstøtte når consumer-response er FAILURE`() {
        Mockito.`when`(kontantstøtteConsumerMock.hentKontantstotte(any()))
            .thenReturn(RestResponse.Failure("Feilmelding", HttpStatus.NOT_FOUND, HttpClientErrorException(HttpStatus.NOT_FOUND)))

        val kontantstøtteRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val kontantstøtteListe = hentKontantstøtteService.hentKontantstøtte(
            kontantstøtteRequestListe = kontantstøtteRequestListe,
            emptyMap(),
        )

        Mockito.verify(kontantstøtteConsumerMock, Mockito.times(1)).hentKontantstotte(any())

        assertAll(
            { assertThat(kontantstøtteListe).isNotNull() },
            { assertThat(kontantstøtteListe).isEmpty() },
        )
    }
}
