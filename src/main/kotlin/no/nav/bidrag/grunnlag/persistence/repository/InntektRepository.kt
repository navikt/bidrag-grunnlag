package no.nav.bidrag.grunnlag.persistence.repository

import no.nav.bidrag.grunnlag.persistence.entity.Inntekt
import org.springframework.data.jpa.repository.JpaRepository

interface InntektRepository : JpaRepository<Inntekt, Int?>{
}