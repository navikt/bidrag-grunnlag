package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
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
class HentRelatertePersonerServiceMockTest {

    @InjectMocks
    private lateinit var hentRelatertePersonerService: HentRelatertePersonerService

    @Mock
    private lateinit var bidragPersonConsumerMock: BidragPersonConsumer

    @Test
    fun `Skal returnere grunnlag når consumer-response er SUCCESS`() {
        Mockito.`when`(bidragPersonConsumerMock.hentHusstandsmedlemmer(any()))
            .thenReturn(RestResponse.Success(TestUtil.byggHentHusstandsmedlemmerResponse()))
        Mockito.`when`(bidragPersonConsumerMock.hentForelderBarnRelasjon(any()))
            .thenReturn(RestResponse.Success(TestUtil.byggHentForelderBarnRelasjonerResponse()))
        Mockito.`when`(bidragPersonConsumerMock.hentNavnFoedselOgDoed(any()))
            .thenReturn(RestResponse.Success(TestUtil.byggHentNavnFoedselOgDoedResponse()))

        val relatertPersonRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val relatertPersonListe = hentRelatertePersonerService.hentRelatertePersoner(relatertPersonRequestListe = relatertPersonRequestListe)

        Mockito.verify(bidragPersonConsumerMock, Mockito.times(1)).hentHusstandsmedlemmer(any())
        Mockito.verify(bidragPersonConsumerMock, Mockito.times(1)).hentForelderBarnRelasjon(any())
        Mockito.verify(bidragPersonConsumerMock, Mockito.times(3)).hentNavnFoedselOgDoed(any())

        assertAll(
            { assertThat(relatertPersonListe).isNotNull() },
            { assertThat(relatertPersonListe).hasSize(5) },
            { assertThat(relatertPersonListe[0].relatertPersonPersonId).isEqualTo("111") },
            { assertThat(relatertPersonListe[1].relatertPersonPersonId).isEqualTo("333") },
            { assertThat(relatertPersonListe[2].relatertPersonPersonId).isEqualTo("444") },
            { assertThat(relatertPersonListe[3].relatertPersonPersonId).isEqualTo("555") },
            { assertThat(relatertPersonListe[4].relatertPersonPersonId).isEqualTo("222") },
            { assertThat(relatertPersonListe[0].borISammeHusstandDtoListe).isNotEmpty },
            { assertThat(relatertPersonListe[1].borISammeHusstandDtoListe).isNotEmpty },
            { assertThat(relatertPersonListe[2].borISammeHusstandDtoListe).isNotEmpty },
            { assertThat(relatertPersonListe[3].borISammeHusstandDtoListe).isNotEmpty },
            { assertThat(relatertPersonListe[4].borISammeHusstandDtoListe).isEmpty() },
        )
    }

    @Test
    fun `Skal returnere tomt grunnlag fra relatertePersoner når consumer-response er FAILURE`() {
        Mockito.`when`(bidragPersonConsumerMock.hentHusstandsmedlemmer(any()))
            .thenReturn(RestResponse.Failure("Feilmelding", HttpStatus.NOT_FOUND, HttpClientErrorException(HttpStatus.NOT_FOUND)))
        Mockito.`when`(bidragPersonConsumerMock.hentForelderBarnRelasjon(any()))
            .thenReturn(RestResponse.Failure("Feilmelding", HttpStatus.NOT_FOUND, HttpClientErrorException(HttpStatus.NOT_FOUND)))

        val relatertPersonRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val relatertPersonListe = hentRelatertePersonerService.hentRelatertePersoner(
            relatertPersonRequestListe = relatertPersonRequestListe,
        )

        Mockito.verify(bidragPersonConsumerMock, Mockito.times(1)).hentHusstandsmedlemmer(any())

        assertAll(
            { assertThat(relatertPersonListe).isNotNull() },
            { assertThat(relatertPersonListe).isEmpty() },
        )
    }
}
