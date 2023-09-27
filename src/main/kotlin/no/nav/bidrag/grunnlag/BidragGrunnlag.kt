package no.nav.bidrag.grunnlag

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework"])
@SpringBootApplication
class BidragGrunnlag

const val ISSUER = "aad"
val SECURE_LOGGER: Logger = LoggerFactory.getLogger("secureLogger")

fun main(args: Array<String>) {
    val profile = if (args.isEmpty()) LIVE_PROFILE else args[0]
    val app = SpringApplication(BidragGrunnlag::class.java)
    app.setAdditionalProfiles(profile)
    app.run(*args)
}
