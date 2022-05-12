package no.nav.bidrag.grunnlag.persistence.entity

import no.nav.bidrag.grunnlag.bo.BarnBo
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import kotlin.reflect.full.memberProperties

@Entity
data class Barn(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "barn_id")
  val barnId: Int = 0,

  @Column(nullable = false, name = "grunnlagspakke_id")
  val grunnlagspakkeId: Int = 0,

  @Column(nullable = true, name = "person_id_barn")
  val personIdBarn: String? = "",

  @Column(nullable = false, name = "person_id_voksen")
  val personIdVoksen: String = "",

  @Column(nullable = true, name = "navn")
  val navn: String? = null,

  @Column(nullable = true, name = "foedselsdato")
  val foedselsdato: LocalDate? = null,

  @Column(nullable = true, name = "foedselsaar")
  val foedselsaar: String? = null,

  @Column(nullable = true, name = "doedsdato")
  val doedsdato: LocalDate? = null,

  @Column(nullable = false, name = "aktiv")
  val aktiv: Boolean = true,

  @Column(nullable = false, name = "bruk_fra")
  val brukFra: LocalDateTime = LocalDateTime.now(),

  @Column(nullable = true, name = "bruk_til")
  val brukTil: LocalDateTime? = null,

  @Column(nullable = true, name = "opprettet_av")
  val opprettetAv: String? = null,

  @Column(nullable = false, name = "opprettet_tidspunkt")
  val opprettetTidspunkt: LocalDateTime = LocalDateTime.now()
)

fun Barn.toBarnBo() = with(::BarnBo) {
  val propertiesByName = Barn::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toBarnBo)
    }
  })
}
