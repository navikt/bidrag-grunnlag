package no.nav.bidrag.grunnlag

import no.nav.bidrag.grunnlag.BidragGrunnlagTest.Companion.TEST_PROFILE
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.test.context.ActiveProfiles

@SpringBootApplication
@ActiveProfiles(TEST_PROFILE)
@ComponentScan(excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [BidragGrunnlag::class, BidragGrunnlagLocal::class])])
class BidragGrunnlagTest {

    companion object {
        const val TEST_PROFILE = "test"
    }
}

fun main(args: Array<String>) {
    val profile = if (args.isEmpty()) TEST_PROFILE else args[0]
    val app = SpringApplication(BidragGrunnlagTest::class.java)
    app.setAdditionalProfiles(profile)
    app.run(*args)
}
