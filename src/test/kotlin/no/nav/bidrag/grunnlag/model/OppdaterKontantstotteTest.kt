package no.nav.bidrag.grunnlag.model

import no.nav.bidrag.domene.ident.Personident
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.familiekssak.FamilieKsSakConsumer
import no.nav.bidrag.grunnlag.exception.RestResponse
import no.nav.bidrag.grunnlag.model.OppdaterKontantstotteTest.MockitoHelper.any
import no.nav.bidrag.grunnlag.service.PersistenceService
import no.nav.bidrag.transport.person.Identgruppe
import no.nav.bidrag.transport.person.PersonidentDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.springframework.http.HttpStatusCode
import org.springframework.web.client.RestClientException
import java.lang.reflect.Method
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class OppdaterKontantstotteTest {

    @Mock
    private lateinit var persistenceService: PersistenceService

    @Mock
    private lateinit var familieKsSakConsumer: FamilieKsSakConsumer

    @Mock
    private lateinit var bidragPersonConsumer: BidragPersonConsumer

    private lateinit var oppdaterKontantstotte: OppdaterKontantstotte

    @BeforeEach
    fun setUp() {
        oppdaterKontantstotte = OppdaterKontantstotte(
            123,
            LocalDateTime.now(),
            persistenceService,
            familieKsSakConsumer,
            bidragPersonConsumer,
        )
    }

    @Test
    fun `sjekker at historiske identer blir hentet`() {
        val personId = "12345678901"
        val forventedeIdenterReturnert = listOf("12345678901", "12345678902")

        `when`(bidragPersonConsumer.hentPersonidenter(any(), eq(true))).thenReturn(
            RestResponse.Success(
                listOf(
                    PersonidentDto("12345678901", false, Identgruppe.FOLKEREGISTERIDENT),
                    PersonidentDto("12345678902", true, Identgruppe.FOLKEREGISTERIDENT),
                ),
            ),
        )

        // Kaller metoden som skal testes (privat metode vha reflection)
        val historiskeIdenter = invokePrivateMethod(oppdaterKontantstotte, "hentHistoriskeIdenterForPerson", personId)

        // Sjekker at responsen er som forventet
        assertEquals(forventedeIdenterReturnert, historiskeIdenter)

        // Verifiser at consumeren kalles med forventede argumenter
        verify(bidragPersonConsumer, times(1)).hentPersonidenter(Personident(personId), true)
    }

    @Test
    fun `sjekker at innsendt ident brukes hvis ingen historiske identer blir funnet`() {
        val personId = "12345678901"
        val forventedeIdenterReturnert = listOf("12345678901")

        `when`(bidragPersonConsumer.hentPersonidenter(any(), eq(true))).thenReturn(
            RestResponse.Success(emptyList()),
        )

        // Kaller metoden som skal testes (privat metode vha reflection)
        val historiskeIdenter = invokePrivateMethod(oppdaterKontantstotte, "hentHistoriskeIdenterForPerson", personId)

        // Sjekker at responsen er som forventet
        assertEquals(forventedeIdenterReturnert, historiskeIdenter)

        // Verifiser at consumeren kalles med forventede argumenter
        verify(bidragPersonConsumer, times(1)).hentPersonidenter(Personident(personId), true)
    }

    @Test
    fun `sjekker at innsendt ident brukes hvis kall til bidrag-person feiler`() {
        val personId = "12345678901"
        val forventedeIdenterReturnert = listOf("12345678901")

        `when`(bidragPersonConsumer.hentPersonidenter(any(), eq(true))).thenReturn(
            RestResponse.Failure("Kall til tjenesten feilet", HttpStatusCode.valueOf(500), RestClientException("Kall til tjenesten feilet")),
        )

        // Kaller metoden som skal testes (privat metode vha reflection)
        val historiskeIdenter = invokePrivateMethod(oppdaterKontantstotte, "hentHistoriskeIdenterForPerson", personId)

        // Sjekker at responsen er som forventet
        assertEquals(forventedeIdenterReturnert, historiskeIdenter)

        // Verifiser at consumeren kalles med forventede argumenter
        verify(bidragPersonConsumer, times(1)).hentPersonidenter(Personident(personId), true)
    }

    // Bruker reflection for å kalle private metoder
    private fun invokePrivateMethod(klasse: Any, metode: String, vararg args: Any): Any? {
        val method: Method = klasse.javaClass.getDeclaredMethod(metode, *args.map { it.javaClass }.toTypedArray())
        method.isAccessible = true
        return method.invoke(klasse, *args)
    }

    object MockitoHelper {
        fun <T> any(): T = Mockito.any()
    }
}
