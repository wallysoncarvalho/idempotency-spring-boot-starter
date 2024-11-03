package com.carvalho.springidempotencyapi

import com.carvalho.springidempotencyapi.util.CacheBodyHttpServletRequest
import com.carvalho.springidempotencyapi.util.CustomHttpResponseWrapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class IdempotencyAPIFilter(
    private val properties: RestAPIIdempotencyProperties,
    private val idempotencyKeyManager: IdempotencyKeyManager,
    private val idempotencyEntryManager: IdempotencyEntryManager
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val cachedRequest = CacheBodyHttpServletRequest(request)
        val responseWrapper = CustomHttpResponseWrapper(response)

        val key = idempotencyKeyManager.generate(cachedRequest, response) ?: return
        val entry = IdempotencyEntry(key = key, request = cachedRequest.inputStream.readAllBytes())

        if (!idempotencyEntryManager.trySave(entry, response)) return

        runCatching {
            filterChain.doFilter(cachedRequest, responseWrapper)
        }.onFailure {
            idempotencyEntryManager.remove(entry.key)
            throw it
        }

        if (!HttpStatusCode.valueOf(responseWrapper.status).is2xxSuccessful) {
            idempotencyEntryManager.remove(entry.key)
            return
        }

        idempotencyEntryManager.updateResponse(entry, responseWrapper.responseData)

        response.outputStream.write(responseWrapper.responseData)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean = !properties.uriMatch(request.requestURI)
}
