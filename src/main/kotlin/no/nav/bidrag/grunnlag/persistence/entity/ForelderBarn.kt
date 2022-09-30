package no.nav.bidrag.grunnlag.persistence.entity

import java.io.Serializable
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

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

class ForelderBarnPK(val forelder: Int = 0, val barn: Int = 0) : Serializable
