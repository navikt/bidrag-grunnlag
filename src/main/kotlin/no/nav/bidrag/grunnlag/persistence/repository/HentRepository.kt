package no.nav.bidrag.grunnlag.persistence.repository

interface HentRepository<T> {
  fun hent(id: Int): List<T>
}