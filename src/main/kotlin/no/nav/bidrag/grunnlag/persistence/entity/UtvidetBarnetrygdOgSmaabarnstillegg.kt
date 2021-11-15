package no.nav.bidrag.grunnlag.persistence.entity

import no.nav.bidrag.grunnlag.dto.UtvidetBarnetrygdOgSmaabarnstilleggDto
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import kotlin.reflect.full.memberProperties

@Entity
data class UtvidetBarnetrygdOgSmaabarnstillegg(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ubst_id")
  val ubstId: Int = 0,

  @Column(nullable = false, name = "grunnlagspakke_id")
  val grunnlagspakkeId: Int = 0,

  @Column(nullable = false, name = "person_id")
  val personId: String = "",

  @Column(nullable = false, name = "type")
  val type: String = "",

  @Column(nullable = false, name = "periode_fra")
  val periodeFra: LocalDate = LocalDate.now(),

  @Column(nullable = false, name = "periode_til")
  val periodeTil: LocalDate? = LocalDate.now(),

  @Column(nullable = false, name = "aktiv")
  val aktiv: Boolean = true,

  @Column(nullable = false, name = "bruk_fra")
  val brukFra: LocalDateTime = LocalDateTime.now(),

  @Column(nullable = true, name = "bruk_til")
  val brukTil: LocalDateTime? = null,

  @Column(nullable = false, name = "belop")
  val belop: BigDecimal = BigDecimal.ZERO,

  @Column(nullable = false, name = "manuelt_beregnet")
  val manueltBeregnet: Boolean = false,

  @Column(nullable = false, name = "delt_bosted")
  val deltBosted: Boolean = false,

  @Column(nullable = false, name = "hentet_tidspunkt")
  val hentetTidspunkt: LocalDateTime = LocalDateTime.now(),
)

fun UtvidetBarnetrygdOgSmaabarnstillegg.toUtvidetBarnetrygdOgSmaabarnstilleggDto() = with(::UtvidetBarnetrygdOgSmaabarnstilleggDto) {
  val propertiesByName = UtvidetBarnetrygdOgSmaabarnstillegg::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      else -> propertiesByName[parameter.name]?.get(this@toUtvidetBarnetrygdOgSmaabarnstilleggDto)
    }
  })
}


