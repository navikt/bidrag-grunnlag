package no.nav.bidrag.grunnlag.service

import io.micrometer.core.instrument.MeterRegistry
import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.grunnlag.TestUtil
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.transport.behandling.grunnlag.request.OppdaterGrunnlagspakkeRequestDto
import no.nav.bidrag.transport.person.Identgruppe
import no.nav.bidrag.transport.person.PersonidentDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.springframework.http.HttpStatusCode
import org.springframework.web.client.RestClientException
import java.lang.reflect.Method

@ExtendWith(MockitoExtension::class)
class HentAktivIdentTest {

    @InjectMocks
    private lateinit var grunnlagspakkeService: GrunnlagspakkeService

    @Mock
    private lateinit var persistenceServiceMock: PersistenceService

    @Mock
    private lateinit var meterRegistry: MeterRegistry

    @Mock
    private lateinit var oppdaterGrunnlagspakkeService: OppdaterGrunnlagspakkeService

    @Mock
    private lateinit var bidragPersonConsumer: BidragPersonConsumer

    @Test
    fun `sjekker at innsendt ident byttes ut med aktiv ident`() {
        val request = TestUtil.byggOppdaterGrunnlagspakkeRequestKontantstotte()
        val innsendtIdent = request.grunnlagRequestDtoListe[0].personId
        val forventetAktivIdent = "12345678911"

        `when`(bidragPersonConsumer.hentPersonidenter(eq(Personident(innsendtIdent)), eq(false))).thenReturn(
            RestResponse.Success(
                listOf(
                    PersonidentDto(forventetAktivIdent, false, Identgruppe.FOLKEREGISTERIDENT),
                ),
            ),
        )

        // Kaller metoden som skal testes (privat metode vha reflection)
        val modifisertRequest: OppdaterGrunnlagspakkeRequestDto =
            invokePrivateMethod(grunnlagspakkeService, "oppdaterAktivIdent", request) as OppdaterGrunnlagspakkeRequestDto

        // Verifiser at consumeren kalles med forventede argumenter
        verify(bidragPersonConsumer, times(1)).hentPersonidenter(Personident(innsendtIdent), false)

        // Sjekker at responsen er som forventet
        assertAll(
            { assertThat(modifisertRequest).isNotNull() },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe.size).isEqualTo(request.grunnlagRequestDtoListe.size) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].personId).isEqualTo(forventetAktivIdent) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].personId).isNotEqualTo(request.grunnlagRequestDtoListe[0].personId) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].periodeFra).isEqualTo(request.grunnlagRequestDtoListe[0].periodeFra) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].periodeTil).isEqualTo(request.grunnlagRequestDtoListe[0].periodeTil) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].type).isEqualTo(request.grunnlagRequestDtoListe[0].type) },
        )
    }

    @Test
    fun `sjekker at innsendt ident beholdes hvis den er lik aktiv ident`() {
        val request = TestUtil.byggOppdaterGrunnlagspakkeRequestKontantstotte()
        val innsendtIdent = request.grunnlagRequestDtoListe[0].personId
        val forventetAktivIdent = request.grunnlagRequestDtoListe[0].personId

        `when`(bidragPersonConsumer.hentPersonidenter(eq(Personident(innsendtIdent)), eq(false))).thenReturn(
            RestResponse.Success(
                listOf(
                    PersonidentDto(forventetAktivIdent, false, Identgruppe.FOLKEREGISTERIDENT),
                ),
            ),
        )

        // Kaller metoden som skal testes (privat metode vha reflection)
        val modifisertRequest: OppdaterGrunnlagspakkeRequestDto =
            invokePrivateMethod(grunnlagspakkeService, "oppdaterAktivIdent", request) as OppdaterGrunnlagspakkeRequestDto

        // Verifiser at consumeren kalles med forventede argumenter
        verify(bidragPersonConsumer, times(1)).hentPersonidenter(Personident(innsendtIdent), false)

        // Sjekker at responsen er som forventet
        assertAll(
            { assertThat(modifisertRequest).isNotNull() },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe.size).isEqualTo(request.grunnlagRequestDtoListe.size) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].personId).isEqualTo(forventetAktivIdent) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].personId).isEqualTo(request.grunnlagRequestDtoListe[0].personId) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].periodeFra).isEqualTo(request.grunnlagRequestDtoListe[0].periodeFra) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].periodeTil).isEqualTo(request.grunnlagRequestDtoListe[0].periodeTil) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].type).isEqualTo(request.grunnlagRequestDtoListe[0].type) },
        )
    }

    @Test
    fun `sjekker at innsendt ident byttes ut med aktiv ident med to personer som input`() {
        val request = TestUtil.byggOppdaterGrunnlagspakkeRequestKontantstotteForToPersoner()
        val innsendtIdent1 = request.grunnlagRequestDtoListe[0].personId
        val innsendtIdent2 = request.grunnlagRequestDtoListe[1].personId
        val forventetAktivIdent1 = "12345678911"
        val forventetAktivIdent2 = request.grunnlagRequestDtoListe[1].personId

        `when`(bidragPersonConsumer.hentPersonidenter(eq(Personident(innsendtIdent1)), eq(false))).thenReturn(
            RestResponse.Success(
                listOf(
                    PersonidentDto(forventetAktivIdent1, false, Identgruppe.FOLKEREGISTERIDENT),
                ),
            ),
        )

        `when`(bidragPersonConsumer.hentPersonidenter(eq(Personident(innsendtIdent2)), eq(false))).thenReturn(
            RestResponse.Success(
                listOf(
                    PersonidentDto(forventetAktivIdent2, false, Identgruppe.FOLKEREGISTERIDENT),
                ),
            ),
        )

        // Kaller metoden som skal testes (privat metode vha reflection)
        val modifisertRequest: OppdaterGrunnlagspakkeRequestDto =
            invokePrivateMethod(grunnlagspakkeService, "oppdaterAktivIdent", request) as OppdaterGrunnlagspakkeRequestDto

        // Verifiser at consumeren kalles med forventede argumenter
        verify(bidragPersonConsumer, times(1)).hentPersonidenter(Personident(innsendtIdent1), false)
        verify(bidragPersonConsumer, times(1)).hentPersonidenter(Personident(innsendtIdent2), false)

        // Sjekker at responsen er som forventet
        assertAll(
            { assertThat(modifisertRequest).isNotNull() },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe.size).isEqualTo(request.grunnlagRequestDtoListe.size) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].personId).isEqualTo(forventetAktivIdent1) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].personId).isNotEqualTo(request.grunnlagRequestDtoListe[0].personId) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].periodeFra).isEqualTo(request.grunnlagRequestDtoListe[0].periodeFra) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].periodeTil).isEqualTo(request.grunnlagRequestDtoListe[0].periodeTil) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].type).isEqualTo(request.grunnlagRequestDtoListe[0].type) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[1].personId).isEqualTo(forventetAktivIdent2) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[1].personId).isEqualTo(request.grunnlagRequestDtoListe[1].personId) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[1].periodeFra).isEqualTo(request.grunnlagRequestDtoListe[1].periodeFra) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[1].periodeTil).isEqualTo(request.grunnlagRequestDtoListe[1].periodeTil) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[1].type).isEqualTo(request.grunnlagRequestDtoListe[1].type) },
        )
    }

    @Test
    fun `sjekker at innsendt ident beholdes hvis kall til bidrag-person feiler`() {
        val request = TestUtil.byggOppdaterGrunnlagspakkeRequestKontantstotte()
        val innsendtIdent = request.grunnlagRequestDtoListe[0].personId
        val forventetAktivIdent = request.grunnlagRequestDtoListe[0].personId

        `when`(bidragPersonConsumer.hentPersonidenter(eq(Personident(innsendtIdent)), eq(false))).thenReturn(
            RestResponse.Failure("Kall til tjenesten feilet", HttpStatusCode.valueOf(500), RestClientException("Kall til tjenesten feilet")),
        )

        // Kaller metoden som skal testes (privat metode vha reflection)
        val modifisertRequest: OppdaterGrunnlagspakkeRequestDto =
            invokePrivateMethod(grunnlagspakkeService, "oppdaterAktivIdent", request) as OppdaterGrunnlagspakkeRequestDto

        // Verifiser at consumeren kalles med forventede argumenter
        verify(bidragPersonConsumer, times(1)).hentPersonidenter(Personident(innsendtIdent), false)

        // Sjekker at responsen er som forventet
        assertAll(
            { assertThat(modifisertRequest).isNotNull() },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe.size).isEqualTo(request.grunnlagRequestDtoListe.size) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].personId).isEqualTo(forventetAktivIdent) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].personId).isEqualTo(request.grunnlagRequestDtoListe[0].personId) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].periodeFra).isEqualTo(request.grunnlagRequestDtoListe[0].periodeFra) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].periodeTil).isEqualTo(request.grunnlagRequestDtoListe[0].periodeTil) },
            { assertThat(modifisertRequest.grunnlagRequestDtoListe[0].type).isEqualTo(request.grunnlagRequestDtoListe[0].type) },
        )
    }

    // Bruker reflection for å kalle private metoder
    private fun invokePrivateMethod(klasse: Any, metode: String, vararg args: Any): Any? {
        val method: Method = klasse.javaClass.getDeclaredMethod(metode, *args.map { it.javaClass }.toTypedArray())
        method.isAccessible = true
        return method.invoke(klasse, *args)
    }
}
