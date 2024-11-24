package com.carvalho.idempotency

import com.carvalho.idempotency.util.CacheBodyServletResponse
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus

class Entry(
    val key: String,
    val requestBody: ByteArray
) {
    private var responseBody: ByteArray? = null

    private var responseHeaders: Map<String, String> = emptyMap()

    private var responseStatus: HttpStatus = HttpStatus.OK

    private var contentType: String = "application/json"

    fun updateWith(responseWrapper: CacheBodyServletResponse) {
        responseBody = responseWrapper.responseData
        responseHeaders = responseWrapper.headerNames.associateWith { responseWrapper.getHeader(it) }
        responseStatus = HttpStatus.valueOf(responseWrapper.status)
        contentType = responseWrapper.contentType
    }

    fun updateServlet(responseWrapper: CacheBodyServletResponse) {
        responseWrapper.response.contentType = contentType
        (responseWrapper.response as HttpServletResponse).status = responseStatus.value()
        responseHeaders.forEach { (key, value) ->
            (responseWrapper.response as HttpServletResponse).setHeader(key, value)
        }
        responseWrapper.response.outputStream.write(responseBody!!)
    }

    fun hasResponse(): Boolean = responseBody != null

    infix fun isDifferentFrom(other: Entry): Boolean {
        return key != other.key || requestBody.contentEquals(other.requestBody).not()
    }
}
