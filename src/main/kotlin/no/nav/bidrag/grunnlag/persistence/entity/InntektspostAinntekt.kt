package no.nav.bidrag.grunnlag.persistence.entity

import no.nav.bidrag.grunnlag.dto.InntektspostAinntektDto
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import kotlin.reflect.full.memberProperties

@Entity
data class InntektspostAinntekt(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "inntektspost_id")
  val inntektspostId: Int = 0,

  @Column(nullable = false, name = "inntekt_id")
  val inntektId: Int = 0,

  @Column(nullable = false, name = "utbetalingsperiode")
  val utbetalingsperiode: String? = "",

  @Column(nullable = true, name = "opptjeningsperiode_fra")
  val opptjeningsperiodeFra: LocalDate? = LocalDate.now(),

  @Column(nullable = true, name = "opptjeningsperiode_til")
  val opptjeningsperiodeTil: LocalDate? = LocalDate.now(),

  @Column(nullable = false, name = "opplysningspliktig_id")
  val opplysningspliktigId: String? = "",

  @Column(nullable = false, name = "inntekt_type")
  val inntektType: String = "",

  @Column(nullable = false, name = "fordel_type")
  val fordelType: String? = "",

  @Column(nullable = false, name = "beskrivelse")
  val beskrivelse: String? = "",

  @Column(nullable = false, name = "belop")
  val belop: BigDecimal = BigDecimal.ZERO
)

fun InntektspostAinntekt.toInntektspostAinntektDto() = with(::InntektspostAinntektDto) {
  val propertiesByName = InntektspostAinntekt::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toInntektspostAinntektDto)
    }
  })
}


