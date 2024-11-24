package com.carvalho.idempotency.util

import jakarta.servlet.ServletOutputStream
import jakarta.servlet.WriteListener
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpServletResponseWrapper
import java.io.ByteArrayOutputStream
import java.io.PrintWriter

class CacheBodyServletResponse(response: HttpServletResponse?) : HttpServletResponseWrapper(response) {
    private val outputStream = ByteArrayOutputStream()
    private val printWriter = PrintWriter(outputStream)

    override fun getOutputStream(): ServletOutputStream = object : ServletOutputStream() {
        override fun isReady(): Boolean = true

        override fun setWriteListener(writeListener: WriteListener) {}

        override fun write(b: Int) {
            outputStream.write(b)
        }
    }

    override fun getWriter(): PrintWriter = printWriter

    val responseData: ByteArray
        get() {
            printWriter.flush()
            return outputStream.toByteArray()
        }
}
