package no.nav.bidrag.grunnlag.persistence.entity

import no.nav.bidrag.grunnlag.bo.HusstandsmedlemBo
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import kotlin.reflect.full.memberProperties

@Entity
data class Husstandsmedlem(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "husstandsmedlem_id")
  val husstandsmedlemId: Int = 0,

  @Column(nullable = false, name = "husstand_id")
  val husstandId: Int = 0,

  @Column(nullable = true, name = "person_id")
  val personId: String? = null,

  @Column(nullable = true, name = "navn")
  val navn: String? = null,

  @Column(nullable = false, name = "periode_fra")
  val periodeFra: LocalDate = LocalDate.now(),

  @Column(nullable = true, name = "periode_til")
  val periodeTil: LocalDate? = null,

  @Column(nullable = true, name = "opprettet_av")
  val opprettetAv: String? = null,

  @Column(nullable = false, name = "opprettet_tidspunkt")
  val opprettetTidspunkt: LocalDateTime = LocalDateTime.now()
)


fun Husstandsmedlem.toHusstandsmedlemBo() = with(::HusstandsmedlemBo) {
  val propertiesByName = Husstandsmedlem::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toHusstandsmedlemBo)
    }
  })
}
