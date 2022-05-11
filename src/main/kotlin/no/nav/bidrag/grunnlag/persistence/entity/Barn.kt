package no.nav.bidrag.grunnlag.persistence.entity

import no.nav.bidrag.grunnlag.bo.BarnBo
import no.nav.bidrag.grunnlag.bo.PersonBo
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

  @Column(nullable = true, name = "person_id")
  val personId: String? = "",

  @Column(nullable = false, name = "person_db_id")
  val personDbId: Int = 0,

  @Column(nullable = true, name = "navn")
  val navn: String? = null,

  @Column(nullable = true, name = "foedselsdato")
  val foedselsdato: LocalDate? = null,

  @Column(nullable = true, name = "doedsdato")
  val doedsdato: LocalDate? = null,

  @Column(nullable = true, name = "opprettet_av")
  val opprettetAv: String? = null,

  @Column(nullable = false, name = "lagret_tidspunkt")
  val lagretTidspunkt: LocalDateTime = LocalDateTime.now()
)

fun Barn.toBarnBo() = with(::BarnBo) {
  val propertiesByName = Barn::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toBarnBo)
    }
  })
}
