package no.nav.bidrag.grunnlag

import no.nav.bidrag.commons.web.test.HttpHeaderTestRestTemplate
import no.nav.bidrag.grunnlag.BidragGrunnlagLocal.Companion.TEST_PROFILE
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders


@Configuration
@Profile(TEST_PROFILE)
class BidragGrunnlagTestConfig {

    @Autowired
    private var mockOAuth2Server: MockOAuth2Server? = null

    @Bean
    fun securedTestRestTemplate(testRestTemplate: TestRestTemplate?): HttpHeaderTestRestTemplate? {
        val httpHeaderTestRestTemplate = HttpHeaderTestRestTemplate(testRestTemplate)
        httpHeaderTestRestTemplate.add(HttpHeaders.AUTHORIZATION) { generateTestToken() }
        return httpHeaderTestRestTemplate
    }

    private fun generateTestToken(): String {
        val token = mockOAuth2Server?.issueToken(ISSUER, "aud-localhost", "aud-localhost")
        return "Bearer " + token?.serialize()
    }
}
