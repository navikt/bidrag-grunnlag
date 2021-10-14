package no.nav.bidrag.grunnlag

import no.nav.bidrag.grunnlag.BidragGrunnlagLocal.Companion.TEST_PROFILE
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = [BidragGrunnlagLocal::class])
@ActiveProfiles(TEST_PROFILE)
@DisplayName("BidragGrunnlag")
@Disabled
class BidragGrunnlagApplicationTest {

  @Test
  fun `skal laste spring-context`() {
  }
}
