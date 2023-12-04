package no.nav.bidrag.grunnlag.service

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.stereotype.Service

@Service("grunnlagSecurityTokenService")
class SecurityTokenService(val authorizedClientManager: OAuth2AuthorizedClientManager) {

    private val anonymousAuthentication: Authentication = AnonymousAuthenticationToken(
        "anonymous",
        "anonymousUser",
        AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"),
    )

    fun generateBearerToken(clientRegistrationId: String): ClientHttpRequestInterceptor? {
        return ClientHttpRequestInterceptor { request: HttpRequest, body: ByteArray?, execution: ClientHttpRequestExecution ->
            val accessToken = authorizedClientManager
                .authorize(
                    OAuth2AuthorizeRequest
                        .withClientRegistrationId(clientRegistrationId)
                        .principal(anonymousAuthentication)
                        .build(),
                )!!.accessToken

            request.headers.setBearerAuth(accessToken.tokenValue)
            execution.execute(request, body!!)
        }
    }
}
