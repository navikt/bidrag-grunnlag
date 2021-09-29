package no.nav.bidrag.grunnlag.service

import no.nav.bidrag.grunnlag.BidragGrunnlagLocal
import no.nav.bidrag.grunnlag.api.OpprettGrunnlagspakkeRequest
import no.nav.bidrag.grunnlag.persistence.repository.GrunnlagspakkeRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@DisplayName("GrunnlagspakkeServiceTest")
@ActiveProfiles(BidragGrunnlagLocal.TEST_PROFILE)
@SpringBootTest(
  classes = [BidragGrunnlagLocal::class],
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class GrunnlagspakkeServiceTest {

  @Autowired
  private lateinit var grunnlagspakkeRepository: GrunnlagspakkeRepository

  @Autowired
  private lateinit var grunnlagspakkeService: GrunnlagspakkeService

  @Autowired
  private lateinit var persistenceService: PersistenceService

  @BeforeEach
  fun `init`() {
    // Sletter alle forekomster
    grunnlagspakkeRepository.deleteAll()
  }

  @Test
  @Suppress("NonAsciiCharacters")
  fun `skal opprette ny grunnlagspakke`() {
    // Oppretter ny grunnlagspakke

    val opprettGrunnlagspakkeRequest = OpprettGrunnlagspakkeRequest(
      "X123456"
    )

    val nyGrunnlagspakkeOpprettet =
      grunnlagspakkeService.opprettGrunnlagspakke(opprettGrunnlagspakkeRequest)

    assertAll(
      Executable { assertThat(nyGrunnlagspakkeOpprettet).isNotNull }

    )

  }



}