package no.nav.bidrag.grunnlag.persistence.entity

import no.nav.bidrag.grunnlag.dto.GrunnlagspakkeDto
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import kotlin.reflect.full.memberProperties

@Entity
data class Grunnlagspakke(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "grunnlagspakke_id")
  val grunnlagspakkeId: Int = 0,

  @Column(nullable = false, name = "opprettet_av")
  val opprettetAv: String = "",

  @Column(nullable = false, name = "opprettet_timestamp")
  val opprettetTimestamp: LocalDateTime = LocalDateTime.now(),

  @Column(nullable = true, name = "endret_timestamp")
  val endretTimestamp: LocalDateTime? = null,

  @Column(nullable = true, name = "gyldig_til")
  val gyldigTil: LocalDate? = null,
)

fun Grunnlagspakke.toGrunnlagspakkeDto() = with(::GrunnlagspakkeDto) {
  val propertiesByName = Grunnlagspakke::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toGrunnlagspakkeDto)
    }
  })
}


