package no.nav.bidrag.grunnlag.persistence.entity

import no.nav.bidrag.grunnlag.dto.InntektAinntektDto
import no.nav.bidrag.grunnlag.dto.InntektSkattDto
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import kotlin.reflect.full.memberProperties

@Entity
data class InntektAinntekt(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "inntekt_id")
  val inntektId: Int = 0,

  @Column(nullable = false, name = "grunnlagspakke_id")
  val grunnlagspakkeId: Int = 0,

  @Column(nullable = false, name = "person_id")
  val personId: Int = 0,

  @Column(nullable = false, name = "periode_fra")
  val periodeFra: LocalDate = LocalDate.now(),

  @Column(nullable = false, name = "periode_til")
  val periodeTil: LocalDate = LocalDate.now(),

  @Column(nullable = false, name = "aktiv")
  val aktiv: Boolean = true,

  @Column(nullable = false, name = "bruk_fra")
  val brukFra: LocalDateTime = LocalDateTime.now(),

  @Column(nullable = true, name = "bruk_til")
  val brukTil: LocalDateTime? = null,

  @Column(nullable = false, name = "hentet_tidspunkt")
  val hentetTidspunkt: LocalDateTime = LocalDateTime.now(),
)

fun InntektAinntekt.toInntektAinntektDto() = with(::InntektAinntektDto) {
  val propertiesByName = InntektAinntekt::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toInntektAinntektDto)
    }
  })
}


