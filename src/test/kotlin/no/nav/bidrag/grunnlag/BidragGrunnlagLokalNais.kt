package no.nav.bidrag.grunnlag

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Profile

@SpringBootApplication
@EnableJwtTokenValidation(ignore = ["org.springdoc", "org.springframework"])
@ComponentScan(excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [BidragGrunnlag::class])])
@Profile("lokal-nais")
class BidragGrunnlagLokalNais

fun main(args: Array<String>) {
    val app = SpringApplication(BidragGrunnlagLokalNais::class.java)
    app.setAdditionalProfiles("lokal-nais", "lokal-nais-secrets")
    app.run(*args)
}
