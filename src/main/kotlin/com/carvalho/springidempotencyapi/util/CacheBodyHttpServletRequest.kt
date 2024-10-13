package com.carvalho.springidempotencyapi.util

import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.springframework.util.StreamUtils
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader

class CacheBodyHttpServletRequest(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
    private val cacheBody: ByteArray

    init {
        val requestInputStream: InputStream = request.inputStream
        this.cacheBody = StreamUtils.copyToByteArray(requestInputStream)
    }

    override fun getInputStream(): ServletInputStream {
        return CacheBodyServletInputStream(ByteArrayInputStream(this.cacheBody))
    }

    override fun getReader(): BufferedReader {
        val byteArrayInputStream = ByteArrayInputStream(this.cacheBody)
        return BufferedReader(InputStreamReader(byteArrayInputStream))
    }
}
