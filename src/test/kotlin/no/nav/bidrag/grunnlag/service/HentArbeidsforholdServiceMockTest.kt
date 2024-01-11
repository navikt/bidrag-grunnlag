package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.ArbeidsforholdConsumer
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.EnhetsregisterConsumer
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
class HentArbeidsforholdServiceMockTest {

    @InjectMocks
    private lateinit var hentArbeidsforholdService: HentArbeidsforholdService

    @Mock
    private lateinit var arbeidsforholdConsumerMock: ArbeidsforholdConsumer

    @Mock
    private lateinit var enhetsregisterConsumerMock: EnhetsregisterConsumer

    @Test
    fun `Skal returnere grunnlag når consumer-response er SUCCESS`() {
        Mockito.`when`(arbeidsforholdConsumerMock.hentArbeidsforhold(any())).thenReturn(RestResponse.Success(TestUtil.byggArbeidsforholdResponse()))

        val arbeidsforholdRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val arbeidsforholdListe = hentArbeidsforholdService.hentArbeidsforhold(
            arbeidsforholdRequestListe = arbeidsforholdRequestListe,
        )

        Mockito.verify(arbeidsforholdConsumerMock, Mockito.times(1)).hentArbeidsforhold(any())

        assertAll(
            { assertThat(arbeidsforholdListe).isNotNull() },
            { assertThat(arbeidsforholdListe).hasSize(2) },
        )
    }

    @Test
    fun `Skal returnere tomt grunnlag fra arbeidsforhold når consumer-response er FAILURE`() {
        Mockito.`when`(arbeidsforholdConsumerMock.hentArbeidsforhold(any()))
            .thenReturn(RestResponse.Failure("Feilmelding", HttpStatus.NOT_FOUND, HttpClientErrorException(HttpStatus.NOT_FOUND)))

        val arbeidsforholdRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val arbeidsforholdListe = hentArbeidsforholdService.hentArbeidsforhold(
            arbeidsforholdRequestListe = arbeidsforholdRequestListe,
        )

        Mockito.verify(arbeidsforholdConsumerMock, Mockito.times(1)).hentArbeidsforhold(any())

        assertAll(
            { assertThat(arbeidsforholdListe).isNotNull() },
            { assertThat(arbeidsforholdListe).isEmpty() },
        )
    }
}
