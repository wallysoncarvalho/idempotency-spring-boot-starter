package com.carvalho.idempotency

import com.carvalho.idempotency.configuration.RestAPIIdempotencyProperties
import com.carvalho.idempotency.util.CacheBodyServletRequest
import com.carvalho.idempotency.util.CacheBodyServletResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

class IdempotencyFilter(
    private val properties: RestAPIIdempotencyProperties,
    private val idempotentRequestHandler: IdempotentRequestHandler,
    private val idempotentResponseHandler: IdempotentResponseHandler
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val cachedRequest = CacheBodyServletRequest(request)
        val cachedResponse = CacheBodyServletResponse(response)

        val entry = idempotentRequestHandler
            .handleIdempotentRequest(cachedRequest, cachedResponse)
            .getOrElse { return }

        val controllerResult = runCatching { filterChain.doFilter(cachedRequest, cachedResponse) }

        idempotentResponseHandler.handle(entry!!, cachedResponse, controllerResult)

        response.outputStream.write(cachedResponse.responseData)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean = !properties.uriMatch(request.requestURI)
}
