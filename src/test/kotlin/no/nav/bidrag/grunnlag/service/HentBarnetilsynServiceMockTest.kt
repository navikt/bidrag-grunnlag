package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.domene.enums.grunnlag.GrunnlagRequestType
import no.nav.bidrag.domene.enums.grunnlag.HentGrunnlagFeiltype
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.familieefsak.FamilieEfSakConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynBisysPerioder
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.BarnetilsynResponse
import no.nav.bidrag.grunnlag.consumer.familieefsak.api.Periode
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.util.GrunnlagUtil.Companion.any
import okhttp3.internal.immutableListOf
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
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class HentBarnetilsynServiceMockTest {

    @InjectMocks
    private lateinit var hentBarnetilsynService: HentBarnetilsynService

    @Mock
    private lateinit var barnetilsynConsumerMock: FamilieEfSakConsumer

    @Test
    fun `Skal returnere grunnlag og ikke feil når consumer-response er SUCCESS`() {
        Mockito.`when`(barnetilsynConsumerMock.hentBarnetilsyn(any())).thenReturn(RestResponse.Success(TestUtil.byggBarnetilsynResponse()))

        val barnetilsynRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val barnetilsynListe = hentBarnetilsynService.hentBarnetilsyn(
            barnetilsynRequestListe = barnetilsynRequestListe,
        )

        Mockito.verify(barnetilsynConsumerMock, Mockito.times(1)).hentBarnetilsyn(any())

        assertAll(
            { assertThat(barnetilsynListe).isNotNull() },
            { assertThat(barnetilsynListe.grunnlagListe).isNotEmpty() },
            { assertThat(barnetilsynListe.grunnlagListe).hasSize(2) },
            { assertThat(barnetilsynListe.grunnlagListe[0].barnPersonId).isEqualTo("01012212345") },
            { assertThat(barnetilsynListe.grunnlagListe[1].barnPersonId).isEqualTo("01011034543") },
            { assertThat(barnetilsynListe.feilrapporteringListe).isEmpty() },
        )
    }

    @Test
    fun `Skal returnere feil og tomt grunnlag fra barnetilsyn når consumer-response er FAILURE`() {
        Mockito.`when`(barnetilsynConsumerMock.hentBarnetilsyn(any()))
            .thenReturn(
                RestResponse.Failure(
                    message = "Ikke funnet",
                    statusCode = HttpStatus.NOT_FOUND,
                    restClientException = HttpClientErrorException(HttpStatus.NOT_FOUND),
                ),
            )

        val barnetilsynRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val barnetilsynListe = hentBarnetilsynService.hentBarnetilsyn(
            barnetilsynRequestListe = barnetilsynRequestListe,
        )

        Mockito.verify(barnetilsynConsumerMock, Mockito.times(1)).hentBarnetilsyn(any())

        assertAll(
            { assertThat(barnetilsynListe).isNotNull() },
            { assertThat(barnetilsynListe.grunnlagListe).isEmpty() },
            { assertThat(barnetilsynListe.feilrapporteringListe).isNotEmpty() },
            { assertThat(barnetilsynListe.feilrapporteringListe).hasSize(1) },
            { assertThat(barnetilsynListe.feilrapporteringListe[0].grunnlagstype).isEqualTo(GrunnlagRequestType.BARNETILSYN) },
            { assertThat(barnetilsynListe.feilrapporteringListe[0].personId).isEqualTo(barnetilsynRequestListe[0].personId) },
            { assertThat(barnetilsynListe.feilrapporteringListe[0].periodeFra).isEqualTo(barnetilsynRequestListe[0].periodeFra) },
            { assertThat(barnetilsynListe.feilrapporteringListe[0].periodeTil).isNull() },
            { assertThat(barnetilsynListe.feilrapporteringListe[0].feiltype).isEqualTo(HentGrunnlagFeiltype.FUNKSJONELL_FEIL) },
            { assertThat(barnetilsynListe.feilrapporteringListe[0].feilmelding).isEqualTo("Ikke funnet") },
        )
    }

    @Test
    fun `Skal filtrere ut barnetilsyn-perioder som ikke overlapper med forespurt periode`() {
        // Response contains periods from 2021-01-01 to 2021-07-31, 2022-01-01 to 2022-12-31, and 2024-06-01 to 2024-12-31
        val barnetilsynResponse = BarnetilsynResponse(
            immutableListOf(
                BarnetilsynBisysPerioder(
                    periode = Periode(
                        fom = LocalDate.parse("2021-01-01"),
                        tom = LocalDate.parse("2021-07-31"),
                    ),
                    barnIdenter = immutableListOf("01012212345"),
                ),
                BarnetilsynBisysPerioder(
                    periode = Periode(
                        fom = LocalDate.parse("2022-01-01"),
                        tom = LocalDate.parse("2022-12-31"),
                    ),
                    barnIdenter = immutableListOf("01011034543"),
                ),
                BarnetilsynBisysPerioder(
                    periode = Periode(
                        fom = LocalDate.parse("2024-06-01"),
                        tom = LocalDate.parse("2024-12-31"),
                    ),
                    barnIdenter = immutableListOf("01012298765"),
                ),
            ),
        )

        Mockito.`when`(barnetilsynConsumerMock.hentBarnetilsyn(any())).thenReturn(RestResponse.Success(barnetilsynResponse))

        // Request period from 2023-01-01 to 2024-12-31 should only include the third period (2024-06-01 to 2024-12-31)
        val barnetilsynRequestListe = listOf(
            PersonIdOgPeriodeRequest(
                personId = "personident",
                periodeFra = LocalDate.parse("2023-01-01"),
                periodeTil = LocalDate.parse("2024-12-31"),
            ),
        )

        val barnetilsynListe = hentBarnetilsynService.hentBarnetilsyn(
            barnetilsynRequestListe = barnetilsynRequestListe,
        )

        Mockito.verify(barnetilsynConsumerMock, Mockito.times(1)).hentBarnetilsyn(any())

        assertAll(
            { assertThat(barnetilsynListe).isNotNull() },
            { assertThat(barnetilsynListe.grunnlagListe).hasSize(1) },
            { assertThat(barnetilsynListe.grunnlagListe[0].barnPersonId).isEqualTo("01012298765") },
            { assertThat(barnetilsynListe.grunnlagListe[0].periodeFra).isEqualTo(LocalDate.parse("2024-06-01")) },
            { assertThat(barnetilsynListe.feilrapporteringListe).isEmpty() },
        )
    }

    @Test
    fun `Skal inkludere barnetilsyn-perioder som delvis overlapper med forespurt periode`() {
        // Period from 2021-06-01 to 2023-06-30 should overlap with request period 2023-01-01 to 2024-01-01
        val barnetilsynResponse = BarnetilsynResponse(
            immutableListOf(
                BarnetilsynBisysPerioder(
                    periode = Periode(
                        fom = LocalDate.parse("2021-06-01"),
                        tom = LocalDate.parse("2023-06-30"),
                    ),
                    barnIdenter = immutableListOf("01012212345", "01011034543"),
                ),
            ),
        )

        Mockito.`when`(barnetilsynConsumerMock.hentBarnetilsyn(any())).thenReturn(RestResponse.Success(barnetilsynResponse))

        val barnetilsynRequestListe = listOf(
            PersonIdOgPeriodeRequest(
                personId = "personident",
                periodeFra = LocalDate.parse("2023-01-01"),
                periodeTil = LocalDate.parse("2024-01-01"),
            ),
        )

        val barnetilsynListe = hentBarnetilsynService.hentBarnetilsyn(
            barnetilsynRequestListe = barnetilsynRequestListe,
        )

        Mockito.verify(barnetilsynConsumerMock, Mockito.times(1)).hentBarnetilsyn(any())

        assertAll(
            { assertThat(barnetilsynListe).isNotNull() },
            { assertThat(barnetilsynListe.grunnlagListe).hasSize(2) },
            { assertThat(barnetilsynListe.grunnlagListe[0].barnPersonId).isEqualTo("01012212345") },
            { assertThat(barnetilsynListe.grunnlagListe[1].barnPersonId).isEqualTo("01011034543") },
            { assertThat(barnetilsynListe.feilrapporteringListe).isEmpty() },
        )
    }
}
