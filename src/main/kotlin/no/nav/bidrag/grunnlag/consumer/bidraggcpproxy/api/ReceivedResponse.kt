package no.nav.bidrag.grunnlag.consumer.bidraggcpproxy.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

sealed class ReceivedResponse<T> {
  data class Success<T>(val body: T) : ReceivedResponse<T>()
  data class Failure<T>(val statusCode: HttpStatus, val body: T) : ReceivedResponse<T>()
}

fun <T> handleResponse(response: ResponseEntity<T>, fallbackBody: T) : ReceivedResponse<T> {
  if (response.statusCode == HttpStatus.OK) {
    return ReceivedResponse.Success(response.body ?: fallbackBody)
  }
  return ReceivedResponse.Failure(response.statusCode, response.body ?: fallbackBody)
}