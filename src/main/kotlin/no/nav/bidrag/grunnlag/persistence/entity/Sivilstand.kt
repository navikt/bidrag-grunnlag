package no.nav.bidrag.grunnlag.persistence.entity

import no.nav.bidrag.grunnlag.bo.SivilstandBo
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import kotlin.reflect.full.memberProperties

@Entity
data class Sivilstand(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "sivilstand_id")
  val sivilstandId: Int = 0,

  @Column(nullable = false, name = "person_db_id")
  val personDbId: String = "",

  @Column(nullable = false, name = "periode_fra")
  val periodeFra: LocalDate = LocalDate.now(),

  @Column(nullable = true, name = "periode_til")
  val periodeTil: LocalDate? = null,

  @Column(nullable = true, name = "sivilstand")
  val sivilstand: String? = null,

  @Column(nullable = true, name = "opprettet_av")
  val opprettetAv: String? = null,

  @Column(nullable = false, name = "lagret_tidspunkt")
  val lagretTidspunkt: LocalDateTime = LocalDateTime.now()
)

fun Sivilstand.toSivilstandBo() = with(::SivilstandBo) {
  val propertiesByName = Sivilstand::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toSivilstandBo)
    }
  })
}
