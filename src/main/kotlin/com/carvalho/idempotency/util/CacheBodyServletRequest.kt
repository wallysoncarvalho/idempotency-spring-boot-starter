package com.carvalho.idempotency.util

import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.springframework.util.StreamUtils
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class CacheBodyServletRequest(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
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

    class CacheBodyServletInputStream(
        private val cacheBodyInputStream: InputStream
    ) : ServletInputStream() {

        /**
         * Indicates whether InputStream has more data to read or not.
         *
         * @return true when zero bytes available to read
         */
        override fun isFinished(): Boolean {
            try {
                return this.cacheBodyInputStream.available() == 0
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return false
        }

        /**
         * Indicates whether InputStream is ready for reading or not.
         * Since we've already copied InputStream in a byte array, we'll return true to indicate that it's always available.
         *
         * @return true
         */
        override fun isReady(): Boolean {
            return true
        }

        override fun setReadListener(listener: ReadListener?) {
            throw UnsupportedOperationException()
        }

        @Throws(IOException::class)
        override fun read(): Int {
            return this.cacheBodyInputStream.read()
        }
    }
}
