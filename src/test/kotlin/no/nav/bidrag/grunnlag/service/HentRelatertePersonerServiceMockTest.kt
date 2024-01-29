package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.domene.enums.grunnlag.GrunnlagRequestType
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
    fun `Skal returnere grunnlag og ikke feil når consumer-response er SUCCESS`() {
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
            { assertThat(relatertPersonListe.grunnlagListe).isNotEmpty() },
            { assertThat(relatertPersonListe.grunnlagListe).hasSize(5) },
            { assertThat(relatertPersonListe.grunnlagListe[0].relatertPersonPersonId).isEqualTo("111") },
            { assertThat(relatertPersonListe.grunnlagListe[1].relatertPersonPersonId).isEqualTo("333") },
            { assertThat(relatertPersonListe.grunnlagListe[2].relatertPersonPersonId).isEqualTo("444") },
            { assertThat(relatertPersonListe.grunnlagListe[3].relatertPersonPersonId).isEqualTo("555") },
            { assertThat(relatertPersonListe.grunnlagListe[4].relatertPersonPersonId).isEqualTo("222") },
            { assertThat(relatertPersonListe.grunnlagListe[0].borISammeHusstandDtoListe).isNotEmpty },
            { assertThat(relatertPersonListe.grunnlagListe[1].borISammeHusstandDtoListe).isNotEmpty },
            { assertThat(relatertPersonListe.grunnlagListe[2].borISammeHusstandDtoListe).isNotEmpty },
            { assertThat(relatertPersonListe.grunnlagListe[3].borISammeHusstandDtoListe).isNotEmpty },
            { assertThat(relatertPersonListe.grunnlagListe[4].borISammeHusstandDtoListe).isEmpty() },
            { assertThat(relatertPersonListe.feilrapporteringListe).isEmpty() },
        )
    }

    @Test
    fun `Skal returnere feil og tomt grunnlag fra relatertePersoner når consumer-response er FAILURE`() {
        Mockito.`when`(bidragPersonConsumerMock.hentHusstandsmedlemmer(any())).thenReturn(
            RestResponse.Failure(
                message = "Ikke funnet",
                statusCode = HttpStatus.NOT_FOUND,
                restClientException = HttpClientErrorException(HttpStatus.NOT_FOUND),
            ),
        )

        Mockito.`when`(bidragPersonConsumerMock.hentForelderBarnRelasjon(any())).thenReturn(
            RestResponse.Failure(
                message = "Ikke funnet",
                statusCode = HttpStatus.NOT_FOUND,
                restClientException = HttpClientErrorException(HttpStatus.NOT_FOUND),
            ),
        )

        val relatertPersonRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val relatertPersonListe = hentRelatertePersonerService.hentRelatertePersoner(
            relatertPersonRequestListe = relatertPersonRequestListe,
        )

        Mockito.verify(bidragPersonConsumerMock, Mockito.times(1)).hentHusstandsmedlemmer(any())

        assertAll(
            { assertThat(relatertPersonListe).isNotNull() },
            { assertThat(relatertPersonListe.grunnlagListe).isEmpty() },
            { assertThat(relatertPersonListe.feilrapporteringListe).isNotEmpty() },
            { assertThat(relatertPersonListe.feilrapporteringListe).hasSize(2) },
            { assertThat(relatertPersonListe.feilrapporteringListe[0].grunnlagstype).isEqualTo(GrunnlagRequestType.HUSSTANDSMEDLEMMER_OG_EGNE_BARN) },
            { assertThat(relatertPersonListe.feilrapporteringListe[0].personId).isEqualTo(relatertPersonRequestListe[0].personId) },
            { assertThat(relatertPersonListe.feilrapporteringListe[0].periodeFra).isNull() },
            { assertThat(relatertPersonListe.feilrapporteringListe[0].periodeTil).isNull() },
            { assertThat(relatertPersonListe.feilrapporteringListe[0].feilkode).isEqualTo(HttpStatus.NOT_FOUND) },
            { assertThat(relatertPersonListe.feilrapporteringListe[0].feilmelding).isEqualTo("Ikke funnet") },
            { assertThat(relatertPersonListe.feilrapporteringListe[1].grunnlagstype).isEqualTo(GrunnlagRequestType.HUSSTANDSMEDLEMMER_OG_EGNE_BARN) },
            { assertThat(relatertPersonListe.feilrapporteringListe[1].personId).isEqualTo(relatertPersonRequestListe[0].personId) },
            { assertThat(relatertPersonListe.feilrapporteringListe[1].periodeFra).isNull() },
            { assertThat(relatertPersonListe.feilrapporteringListe[1].periodeTil).isNull() },
            { assertThat(relatertPersonListe.feilrapporteringListe[1].feilkode).isEqualTo(HttpStatus.NOT_FOUND) },
            { assertThat(relatertPersonListe.feilrapporteringListe[1].feilmelding).isEqualTo("Ikke funnet") },
        )
    }
}
