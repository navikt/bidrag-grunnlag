package no.nav.bidrag.grunnlag.persistence.entity

import no.nav.bidrag.grunnlag.bo.HusstandBo
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import kotlin.reflect.full.memberProperties

@Entity
data class Husstand(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "husstand_id")
  val husstandId: Int = 0,

  @Column(nullable = false, name = "personn_db_id")
  val personnDbId: String = "",

  @Column(nullable = false, name = "periode_fra")
  val periodeFra: LocalDate = LocalDate.now(),

  @Column(nullable = true, name = "periode_til")
  val periodeTil: LocalDate? = null,

  @Column(nullable = true, name = "adressenavn")
  val adressenavn: String? = null,

  @Column(nullable = true, name = "husnummer")
  val husnummer: String? = null,

  @Column(nullable = true, name = "husbokstav")
  val husbokstav: String? = null,

  @Column(nullable = true, name = "bruksenhetsnummer")
  val bruksenhetsnummer: String? = null,

  @Column(nullable = true, name = "postnr")
  val postnr: String? = null,

  @Column(nullable = true, name = "bydelsnummer")
  val bydelsnummer: String? = null,

  @Column(nullable = true, name = "kommunenummer")
  val kommunenummer: String? = null,

  @Column(nullable = true, name = "matrikkelId")
  val matrikkelId: String? = null,

  @Column(nullable = true, name = "opprettet_av")
  val opprettetAv: String? = null,

  @Column(nullable = false, name = "lagret_tidspunkt")
  val lagretTidspunkt: LocalDateTime = LocalDateTime.now()
)


fun Husstand.toHusstandBo() = with(::HusstandBo) {
  val propertiesByName = Husstand::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toHusstandBo)
    }
  })
}
