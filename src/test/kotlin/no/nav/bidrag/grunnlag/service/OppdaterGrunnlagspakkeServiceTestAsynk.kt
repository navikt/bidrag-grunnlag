package no.nav.bidrag.grunnlag.service

import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.test.runTest
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.FamilieEfSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiekssak.FamilieKsSakConsumer
import no.nav.bidrag.grunnlag.consumer.pensjon.PensjonConsumer
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.SigrunConsumer
import no.nav.bidrag.transport.behandling.grunnlag.request.OppdaterGrunnlagspakkeRequestDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class OppdaterGrunnlagspakkeServiceTestAsynk {

    private val persistenceServiceMock = mockk<PersistenceService>()
    private val familieBaSakConsumerMock = mockk<FamilieBaSakConsumer>()
    private val pensjonConsumerMock = mockk<PensjonConsumer>()
    private val inntektskomponentenServiceMock = mockk<InntektskomponentenService>()
    private val sigrunConsumerMock = mockk<SigrunConsumer>()
    private val bidragPersonConsumerMock = mockk<BidragPersonConsumer>()
    private val familieKsSakConsumerMock = mockk<FamilieKsSakConsumer>()
    private val familieEfSakConsumerMock = mockk<FamilieEfSakConsumer>()

    private val oppdaterGrunnlagspakkeService = OppdaterGrunnlagspakkeService(
        persistenceServiceMock,
        familieBaSakConsumerMock,
        pensjonConsumerMock,
        inntektskomponentenServiceMock,
        sigrunConsumerMock,
        bidragPersonConsumerMock,
        familieKsSakConsumerMock,
        familieEfSakConsumerMock
    )

    @Test
    fun `skal kalle alle asynk-tjenester og vente på respons`() = runTest {
        val grunnlagspakkeId = 123
        val oppdaterRequest = mockk<OppdaterGrunnlagspakkeRequestDto>(relaxed = true)
        val timestamp = LocalDateTime.now()
        val historiskeIdenter = mapOf("12345678910" to listOf("10987654321"))

        every {
            persistenceServiceMock.hentFormaalGrunnlagspakke(any())
        } returns "Bidrag"

        // Spioner på OppdaterGrunnlagspakke instans for å følge metodekall
        val oppdaterGrunnlagspakkeSpy = spyk(
            oppdaterGrunnlagspakkeService.OppdaterGrunnlagspakke(grunnlagspakkeId, timestamp)
        )

        // Kall metoden som skal testes
        val respons = oppdaterGrunnlagspakkeService.oppdaterGrunnlagspakke(
            grunnlagspakkeId,
            oppdaterRequest,
            timestamp,
            historiskeIdenter,
            oppdaterGrunnlagspakkeSpy
        )

        // verifiser at alle asynk-tjenester ble kalt og ventet på
        coVerify {
            oppdaterGrunnlagspakkeSpy.oppdaterAinntekt(any(), any())
            oppdaterGrunnlagspakkeSpy.oppdaterSkattegrunnlag(any(), any())
            oppdaterGrunnlagspakkeSpy.oppdaterUtvidetBarnetrygdOgSmaabarnstillegg(any(), any())
            oppdaterGrunnlagspakkeSpy.oppdaterBarnetillegg(any(), any())
            oppdaterGrunnlagspakkeSpy.oppdaterKontantstotte(any(), any())
            oppdaterGrunnlagspakkeSpy.oppdaterHusstandsmedlemmerOgEgneBarn(any(), any())
            oppdaterGrunnlagspakkeSpy.oppdaterSivilstand(any(), any())
            oppdaterGrunnlagspakkeSpy.oppdaterBarnetilsyn(any(), any())
        }

        // Verifiser at resultatet inneholder forventet grunnlagspakkeId
        assertEquals(grunnlagspakkeId, respons.grunnlagspakkeId)
    }
}