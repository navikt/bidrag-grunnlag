package no.nav.bidrag.grunnlag.persistence.entity

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
data class Person(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "person_db_id")
  val personnDbId: Int = 0,

  @Column(nullable = false, name = "grunnlagspakke_id")
  val grunnlagspakkeId: Int = 0,

  @Column(nullable = true, name = "person_id")
  val personId: String? = null,

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

fun Person.toPersonBo() = with(::PersonBo) {
  val propertiesByName = Person::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toPersonBo)
    }
  })
}