package no.nav.bidrag.grunnlag

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation

@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework"])
@SpringBootApplication
class BidragGrunnlag

const val ISSUER = "aad"

fun main(args: Array<String>) {
    val profile = if (args.isEmpty()) LIVE_PROFILE else args[0]
    val app = SpringApplication(BidragGrunnlag::class.java)
    app.setAdditionalProfiles(profile)
    app.run(*args)
}
