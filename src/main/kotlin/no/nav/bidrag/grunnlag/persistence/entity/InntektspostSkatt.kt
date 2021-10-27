package no.nav.bidrag.grunnlag.persistence.entity

import no.nav.bidrag.grunnlag.dto.InntektspostSkattDto
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import kotlin.reflect.full.memberProperties

@Entity
data class InntektspostSkatt(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "inntektspost_id")
  val inntektspostId: Int = 0,

  @Column(nullable = false, name = "inntekt_id")
  val inntektId: Int = 0,

  @Column(nullable = false, name = "type")
  val type: String = "",

  @Column(nullable = false, name = "belop")
  val belop: BigDecimal = BigDecimal.ZERO
)

fun InntektspostSkatt.toInntektspostSkattDto() = with(::InntektspostSkattDto) {
  val propertiesByName = InntektspostSkatt::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toInntektspostSkattDto)
    }
  })
}


