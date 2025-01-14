import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.domene.enums.vedtak.Formål
import no.nav.bidrag.grunnlag.BidragGrunnlagTest
import no.nav.bidrag.grunnlag.BidragGrunnlagTest.Companion.TEST_PROFILE
import no.nav.bidrag.grunnlag.consumer.arbeidsforhold.ArbeidsforholdConsumer
import no.nav.bidrag.grunnlag.consumer.bidragperson.BidragPersonConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.FamilieBaSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiebasak.TilleggsstønadConsumer
import no.nav.bidrag.grunnlag.consumer.familieefsak.FamilieEfSakConsumer
import no.nav.bidrag.grunnlag.consumer.familiekssak.FamilieKsSakConsumer
import no.nav.bidrag.grunnlag.consumer.inntektskomponenten.InntektskomponentenConsumer
import no.nav.bidrag.grunnlag.consumer.pensjon.PensjonConsumer
import no.nav.bidrag.grunnlag.consumer.skattegrunnlag.SigrunConsumer
import no.nav.bidrag.grunnlag.controller.GrunnlagController
import no.nav.bidrag.grunnlag.service.GrunnlagspakkeService
import no.nav.bidrag.grunnlag.service.HentGrunnlagService
import no.nav.bidrag.transport.behandling.grunnlag.request.OpprettGrunnlagspakkeRequestDto
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.util.UriComponentsBuilder

//@ExtendWith(SpringExtension::class)
//@WebMvcTest(GrunnlagController::class)
@DisplayName("GrunnlagControllerTest")
@ActiveProfiles(TEST_PROFILE)
@SpringBootTest(classes = [BidragGrunnlagTest::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = 0)
//@Transactional
//@AutoConfigureMockMvc
class GrunnlagControllerTestNy {

//    @Autowired
//    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var securedTestRestTemplate: HttpHeaderTestRestTemplate

    @MockitoBean
    private lateinit var grunnlagspakkeService: GrunnlagspakkeService

    @MockitoBean
    private lateinit var hentGrunnlagService: HentGrunnlagService

    @MockitoBean
    private lateinit var bidragPersonConsumer: BidragPersonConsumer

    @MockitoBean
    private lateinit var arbeidsforholdConsumer: ArbeidsforholdConsumer

    @MockitoBean
    private lateinit var familieBaSakConsumer: FamilieBaSakConsumer

    @MockitoBean
    private lateinit var tilleggsstønadConsumer: TilleggsstønadConsumer

    @MockitoBean
    private lateinit var familieEfSakConsumer: FamilieEfSakConsumer

    @MockitoBean
    private lateinit var familieKsSakConsumer: FamilieKsSakConsumer

    @MockitoBean
    private lateinit var inntektskomponentenConsumer: InntektskomponentenConsumer

    @MockitoBean
    private lateinit var pensjonConsumer: PensjonConsumer

    @MockitoBean
    private lateinit var sigrunConsumer: SigrunConsumer

    @LocalServerPort
    private val port = 0

    @Test
    fun `skal mappe til context path med random port`() {
        assertThat(makeFullContextPath()).isEqualTo("http://localhost:$port")
    }

    @Test
    fun `skal opprette ny grunnlagspakke`() {
        val request = OpprettGrunnlagspakkeRequestDto(Formål.FORSKUDD, "X123456")
        `when`(grunnlagspakkeService.opprettGrunnlagspakke(request)).thenReturn(1)

//        mockMvc.perform(post("/grunnlagspakke")
//            .contentType("application/json")
//            .content("""{"formaal":"FORSKUDD","ident":"X123456"}"""))
//            .andExpect(status().isOk)
//            .andExpect(content().string("1"))

        val response = securedTestRestTemplate.exchange(
            fullUrlForOpprettGrunnlagspakke(),
            HttpMethod.POST,
            byggHttpOpprettGrunnlagspakkeRequest(request),
            Int::class.java,
        )

        assertAll(
            { assert(response.statusCode.is2xxSuccessful) },
            { assert(response.body == 1) },
        )
    }

    private fun fullUrlForOpprettGrunnlagspakke() = UriComponentsBuilder.fromUriString(makeFullContextPath() + GrunnlagController.GRUNNLAGSPAKKE_NY).toUriString()
    private fun makeFullContextPath() = "http://localhost:$port"
    private fun byggHttpOpprettGrunnlagspakkeRequest(request: OpprettGrunnlagspakkeRequestDto): HttpEntity<OpprettGrunnlagspakkeRequestDto> = initHttpEntity(request)

    private fun <T> initHttpEntity(body: T): HttpEntity<T> {
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(body, httpHeaders)
    }

//    @Test
//    fun `should update grunnlagspakke`() {
//        val request = OppdaterGrunnlagspakkeRequestDto()
//        val response = OppdaterGrunnlagspakkeDto()
//        `when`(grunnlagspakkeService.oppdaterGrunnlagspakke(1, request)).thenReturn(response)
//
//        mockMvc.perform(post("/grunnlagspakke/1/oppdater")
//            .contentType("application/json")
//            .content("""{}"""))
//            .andExpect(status().isOk)
//            .andExpect(content().json("""{}"""))
//    }
//
//    @Test
//    fun `should fetch grunnlagspakke`() {
//        val response = HentGrunnlagspakkeDto()
//        `when`(grunnlagspakkeService.hentGrunnlagspakke(1)).thenReturn(response)
//
//        mockMvc.perform(get("/grunnlagspakke/1"))
//            .andExpect(status().isOk)
//            .andExpect(content().json("""{}"""))
//    }
//
//    @Test
//    fun `should close grunnlagspakke`() {
//        `when`(grunnlagspakkeService.lukkGrunnlagspakke(1)).thenReturn(1)
//
//        mockMvc.perform(post("/grunnlagspakke/1/lukk"))
//            .andExpect(status().isOk)
//            .andExpect(content().string("1"))
//    }
//
//    @Test
//    fun `should fetch grunnlag`() {
//        val request = HentGrunnlagRequestDto()
//        val response = HentGrunnlagDto()
//        `when`(hentGrunnlagService.hentGrunnlag(request)).thenReturn(response)
//
//        mockMvc.perform(post("/hentgrunnlag")
//            .contentType("application/json")
//            .content("""{}"""))
//            .andExpect(status().isOk)
//            .andExpect(content().json("""{}"""))
//    }
}