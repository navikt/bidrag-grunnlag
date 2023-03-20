package no.nav.bidrag.grunnlag.persistence.entity

import no.nav.bidrag.grunnlag.bo.HusstandsmedlemskapBo
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import kotlin.reflect.full.memberProperties

@Entity
data class Husstandsmedlemskap(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "husstandsmedlemskap_id")
  val husstandsmedlemskapId: Int = 0,

  @Column(nullable = false, name = "grunnlagspakke_id")
  val grunnlagspakkeId: Int = 0,

  @Column(nullable = false, name = "part_person_id")
  val partPersonId: String = "",

  @Column(nullable = true, name = "husstandsmedlem_person_id")
  val husstandsmedlemPersonId: String? = null,

  @Column(nullable = true, name = "navn")
  val navn: String? = null,

  @Column(nullable = true, name = "fodselsdato")
  val fodselsdato: LocalDate? = null,

  @Column(nullable = false, name = "er_barn_av_bm_bp")
  val erBarnAvBmBp: Boolean = false,

  @Column(nullable = true, name = "periode_fra")
  val periodeFra: LocalDate? = null,

  @Column(nullable = true, name = "periode_til")
  val periodeTil: LocalDate? = null,

  @Column(nullable = false, name = "aktiv")
  val aktiv: Boolean = true,

  @Column(nullable = false, name = "bruk_fra")
  val brukFra: LocalDateTime = LocalDateTime.now(),

  @Column(nullable = true, name = "bruk_til")
  val brukTil: LocalDateTime? = null,

  @Column(nullable = false, name = "hentet_tidspunkt")
  val hentetTidspunkt: LocalDateTime = LocalDateTime.now()
)


fun Husstandsmedlemskap.toHusstandsmedlemskapBo() = with(::HusstandsmedlemskapBo) {
  val propertiesByName = Husstandsmedlemskap::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toHusstandsmedlemskapBo)
    }
  })
}
