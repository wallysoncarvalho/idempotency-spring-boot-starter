package com.carvalho.springidempotencyapi.util

import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import java.io.IOException
import java.io.InputStream

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
