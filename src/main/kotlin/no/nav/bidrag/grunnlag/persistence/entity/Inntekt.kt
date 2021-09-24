package no.nav.bidrag.grunnlag.persistence.entity

import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import no.nav.bidrag.grunnlag.dto.InntektDto
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import kotlin.reflect.full.memberProperties

@Entity
data class Inntekt(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "inntekt_id")
  val inntektId: Int = 0,

  @Column(nullable = false, name = "grunnlagspakke_id")
  val grunnlagspakkeId: Int = 0,

  @Column(nullable = false, name = "person_id")
  val personId: Int = 0,

  @Column(nullable = false, name = "type")
  val type: String = "",

  @Column(nullable = false, name = "gyldig_fra")
  val gyldigFra: LocalDate = LocalDate.now(),

  @Column(nullable = false, name = "gyldig_til")
  val gyldigTil: LocalDate = LocalDate.now(),

  @Column(nullable = false, name = "aktiv")
  val aktiv: Boolean = true,

  @Column(nullable = false, name = "hentet_tidspunkt")
  val hentetTidspunkt: LocalDateTime = LocalDateTime.now(),

  @Column(nullable = false, name = "bruk_fra")
  val brukFra: LocalDateTime = LocalDateTime.now(),

  @Column(nullable = true, name = "bruk_til")
  val brukTil: LocalDateTime? = null
)

fun Inntekt.toInntektDto() = with(::InntektDto) {
  val propertiesByName = Inntekt::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toInntektDto)
    }
  })
}


