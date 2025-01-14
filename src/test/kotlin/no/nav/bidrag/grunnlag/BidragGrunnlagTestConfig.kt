package no.nav.bidrag.grunnlag

import com.nimbusds.jose.JOSEObjectType
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import no.nav.bidrag.grunnlag.BidragGrunnlagLocal.Companion.LOCAL_PROFILE
import no.nav.bidrag.grunnlag.BidragGrunnlagTest.Companion.TEST_PROFILE
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

@Configuration
@OpenAPIDefinition(
    info = Info(title = "bidrag-grunnlag", version = "v1"),
    security = [SecurityRequirement(name = "bearer-key")],
)
@Profile(TEST_PROFILE, LOCAL_PROFILE)
class BidragGrunnlagTestConfig {

    @Autowired
    private lateinit var mockOAuth2Server: MockOAuth2Server

    @Bean
    fun securedTestRestTemplate(): TestRestTemplate? = TestRestTemplate(
        RestTemplateBuilder()
            .additionalMessageConverters(MappingJackson2HttpMessageConverter())
            .additionalInterceptors({ request, body, execution ->
                request.headers.add(HttpHeaders.AUTHORIZATION, generateTestToken())
                execution.execute(request, body)
            }),
    )

    protected fun generateTestToken(): String {
        val iss = mockOAuth2Server.issuerUrl("aad")
        val newIssuer = iss.newBuilder().host("localhost").build()
        val token =
            mockOAuth2Server.issueToken(
                "aad",
                "aud-localhost",
                DefaultOAuth2TokenCallback(
                    issuerId = "aad",
                    subject = "Z999999",
                    typeHeader = JOSEObjectType.JWT.type,
                    audience = listOf("aud-localhost"),
                    claims = mapOf("iss" to newIssuer.toString()),
                    3600,
                ),
            )
        return "Bearer " + token.serialize()
    }
}
