package com.carvalho.springidempotencyapi

import com.carvalho.springidempotencyapi.LocalIdempotencyAPIRepository.DuplicateStorageKeyException
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
import org.springframework.web.util.ContentCachingResponseWrapper
import java.util.Base64

/**
 * TODO:
 * 1. configure filter on application.yml
 *  - configure which endpoints should be idempotent
 *  - configure which headers should be used as key
 *  -
 * 2. create repository
 * 3. deploy artifact
 * 4. use artifact in another project
 * 5. write tests
 */

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
        val encodedRequest = Base64.getEncoder().encode(cachedRequest.inputStream.readAllBytes()).decodeToString()
        val entry = IdempotencyEntry(key = key, input = encodedRequest)

        if(!idempotencyEntryManager.trySave(entry, response)) return



        runCatching { filterChain.doFilter(cachedRequest, responseWrapper) }.onFailure {
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
