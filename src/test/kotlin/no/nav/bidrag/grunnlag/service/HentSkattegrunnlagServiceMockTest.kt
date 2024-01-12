package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.domene.enums.inntekt.Skattegrunnlagstype
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.SigrunConsumer
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
class HentSkattegrunnlagServiceMockTest {

    @InjectMocks
    private lateinit var hentSkattegrunnlagService: HentSkattegrunnlagService

    @Mock
    private lateinit var sigrunConsumerMock: SigrunConsumer

    @Test
    fun `Skal returnere grunnlag når consumer-response er SUCCESS`() {
        Mockito.`when`(
            sigrunConsumerMock.hentSummertSkattegrunnlag(any()),
        ).thenReturn(RestResponse.Success(TestUtil.byggHentSkattegrunnlagResponse()))

        val skattegrunnlagRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val skattegrunnlagListe = hentSkattegrunnlagService.hentSkattegrunnlag(
            skattegrunnlagRequestListe = skattegrunnlagRequestListe,
        )

        Mockito.verify(sigrunConsumerMock, Mockito.times(1)).hentSummertSkattegrunnlag(any())

        assertAll(
            { assertThat(skattegrunnlagListe).isNotNull() },
            { assertThat(skattegrunnlagListe).hasSize(1) },
            { assertThat(skattegrunnlagListe[0].skattegrunnlagspostListe).hasSize(2) },
            { assertThat(skattegrunnlagListe[0].skattegrunnlagspostListe[0].skattegrunnlagType).isEqualTo(Skattegrunnlagstype.ORDINÆR.toString()) },
            { assertThat(skattegrunnlagListe[0].skattegrunnlagspostListe[1].skattegrunnlagType).isEqualTo(Skattegrunnlagstype.SVALBARD.toString()) },
        )
    }

    @Test
    fun `Skal returnere tomt grunnlag fra skattegrunnlag når consumer-response er FAILURE`() {
        Mockito.`when`(sigrunConsumerMock.hentSummertSkattegrunnlag(any()))
            .thenReturn(RestResponse.Failure("Feilmelding", HttpStatus.NOT_FOUND, HttpClientErrorException(HttpStatus.NOT_FOUND)))

        val skattegrunnlagRequestListe = listOf(TestUtil.byggPersonIdOgPeriodeRequest())

        val skattegrunnlagListe = hentSkattegrunnlagService.hentSkattegrunnlag(
            skattegrunnlagRequestListe = skattegrunnlagRequestListe,
        )

        Mockito.verify(sigrunConsumerMock, Mockito.times(1)).hentSummertSkattegrunnlag(any())

        assertAll(
            { assertThat(skattegrunnlagListe).isNotNull() },
            { assertThat(skattegrunnlagListe).isEmpty() },
        )
    }
}
