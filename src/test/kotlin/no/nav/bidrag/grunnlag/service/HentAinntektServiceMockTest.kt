package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.domene.enums.vedtak.Formål
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.api.HentInntektListeResponseIntern
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

@ExtendWith(MockitoExtension::class)
class HentAinntektServiceMockTest {

    @InjectMocks
    private lateinit var hentAinntektService: HentAinntektService

    @Mock
    private lateinit var inntektskomponentenServiceMock: InntektskomponentenService

    @Test
    fun `Skal returnere grunnlag når InntektskomponentenService-respons er OK`() {
        Mockito.`when`(inntektskomponentenServiceMock.hentInntekt(any())).thenReturn(TestUtil.byggHentInntektListeResponseIntern())

        val ainntektRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val ainntektListe = hentAinntektService.hentAinntekt(
            ainntektRequestListe = ainntektRequestListe,
            formål = Formål.BIDRAG,
        )

        Mockito.verify(inntektskomponentenServiceMock, Mockito.times(1)).hentInntekt(any())

        assertAll(
            { assertThat(ainntektListe).isNotNull() },
            { assertThat(ainntektListe).hasSize(1) },
            { assertThat(ainntektListe[0].ainntektspostListe).isNotEmpty },
            { assertThat(ainntektListe[0].ainntektspostListe).hasSize(1) },
        )
    }

    @Test
    fun `Skal returnere tomt grunnlag fra arbeidsforhold når consumer-response er FAILURE`() {
        Mockito.`when`(inntektskomponentenServiceMock.hentInntekt(any()))
            .thenReturn(HentInntektListeResponseIntern(httpStatus = HttpStatus.NOT_FOUND, arbeidsInntektMaanedIntern = emptyList()))

        val ainntektRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val ainntektListe = hentAinntektService.hentAinntekt(
            ainntektRequestListe = ainntektRequestListe,
            formål = Formål.BIDRAG,
        )

        Mockito.verify(inntektskomponentenServiceMock, Mockito.times(1)).hentInntekt(any())

        assertAll(
            { assertThat(ainntektListe).isNotNull() },
            { assertThat(ainntektListe).isEmpty() },
        )
    }
}
