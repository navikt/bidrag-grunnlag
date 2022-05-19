package no.nav.bidrag.grunnlag.persistence.entity

import no.nav.bidrag.grunnlag.bo.ForelderBarnBo
import java.io.Serializable
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import kotlin.reflect.full.memberProperties

@IdClass(ForelderBarnPK::class)
@Entity
@Table(name = "forelderbarn")
data class ForelderBarn(

  @Id
  @ManyToOne
  @JoinColumn(name = "forelder_id")
  val forelder: Forelder = Forelder(),

  @Id
  @ManyToOne
  @JoinColumn(name = "barn_id")
  val barn: Barn = Barn()

)

fun ForelderBarnBo.toForelderBarnEntity(eksisterendeForelder: Forelder, eksisterendeBarn: Barn) = with(::ForelderBarn) {
  val propertiesByName = ForelderBarnBo::class.memberProperties.associateBy { it.name }
  callBy(parameters.associateWith { parameter ->
    when (parameter.name) {
      ForelderBarn::forelder.name -> eksisterendeForelder
      ForelderBarn::barn.name -> eksisterendeBarn
      else -> propertiesByName[parameter.name]?.get(this@toForelderBarnEntity)
    }
  })
}

class ForelderBarnPK(val forelder: Int = 0, val barn: Int = 0) : Serializable
