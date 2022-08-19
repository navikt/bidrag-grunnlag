package no.nav.bidrag.grunnlag.persistence.entity

import no.nav.bidrag.behandling.felles.enums.barnetilsyn.Skolealder
import no.nav.bidrag.behandling.felles.enums.barnetilsyn.Tilsyntype
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class Barnetilsyn(

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "barnetilsyn_id")
  val barnetilsynId: Int = 0,

  @Column(nullable = false, name = "grunnlagspakke_id")
  val grunnlagspakkeId: Int = 0,

  @Column(nullable = false, name = "part_person_id")
  val partPersonId: String = "",

  @Column(nullable = false, name = "barn_person_id")
  val barnPersonId: String = "",

  @Column(nullable = false, name = "periode_fra")
  val periodeFra: LocalDate = LocalDate.now(),

  @Column(nullable = true, name = "periode_til")
  val periodeTil: LocalDate? = LocalDate.now(),

  @Column(nullable = false, name = "aktiv")
  val aktiv: Boolean = true,

  @Column(nullable = false, name = "bruk_fra")
  val brukFra: LocalDateTime = LocalDateTime.now(),

  @Column(nullable = true, name = "bruk_til")
  val brukTil: LocalDateTime? = null,

  @Column(nullable = false, name = "belop")
  val belop: Int = 0,

  @Column(nullable = true, name = "tilsynstype")
  val tilsynstype: Tilsyntype?,

  @Column(nullable = false, name = "skolealder")
  val skolealder: Skolealder,

  @Column(nullable = false, name = "hentet_tidspunkt")
  val hentetTidspunkt: LocalDateTime = LocalDateTime.now()
)