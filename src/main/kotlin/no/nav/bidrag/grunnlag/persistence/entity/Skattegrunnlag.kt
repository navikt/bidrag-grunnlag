package no.nav.bidrag.grunnlag.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import no.nav.bidrag.grunnlag.bo.SkattegrunnlagBo
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.full.memberProperties

@Entity
data class Skattegrunnlag(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skattegrunnlag_id")
    val skattegrunnlagId: Int = 0,

    @Column(nullable = false, name = "grunnlagspakke_id")
    val grunnlagspakkeId: Int = 0,

    @Column(nullable = false, name = "person_id")
    val personId: String = "",

    @Column(nullable = false, name = "periode_fra")
    val periodeFra: LocalDate = LocalDate.now(),

    @Column(nullable = false, name = "periode_til")
    val periodeTil: LocalDate = LocalDate.now(),

    @Column(nullable = false, name = "aktiv")
    val aktiv: Boolean = true,

    @Column(nullable = false, name = "bruk_fra")
    val brukFra: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = true, name = "bruk_til")
    val brukTil: LocalDateTime? = null,

    @Column(nullable = false, name = "hentet_tidspunkt")
    val hentetTidspunkt: LocalDateTime = LocalDateTime.now(),
)

fun Skattegrunnlag.toSkattegrunnlagBo() = with(::SkattegrunnlagBo) {
    val propertiesByName = Skattegrunnlag::class.memberProperties.associateBy { it.name }
    callBy(
        parameters.associateWith { parameter ->
            when (parameter.name) {
                else -> propertiesByName[parameter.name]?.get(this@toSkattegrunnlagBo)
            }
        },
    )
}
