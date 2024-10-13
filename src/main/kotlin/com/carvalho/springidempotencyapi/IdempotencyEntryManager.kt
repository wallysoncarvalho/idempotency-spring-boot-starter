package com.carvalho.springidempotencyapi

import jakarta.servlet.http.HttpServletResponse
import java.util.Base64

class IdempotencyEntryManager(
    private val storage: IdempotencyAPIRepository,
    private val errorResponseHandler: ErrorResponseHandler
) {

    fun trySave(entry: IdempotencyEntry, response: HttpServletResponse): Boolean {
        return runCatching { storage.save(entry) }.onFailure {
            if (it is LocalIdempotencyAPIRepository.DuplicateStorageKeyException) {
                handleDuplicateKey(entry, response)
                return false
            }
            throw it
        }.isSuccess
    }

    private fun handleDuplicateKey(entry: IdempotencyEntry, response: HttpServletResponse) {
        val existingEntry = storage.getByKey(entry.key)!!

        when {
            existingEntry.output == null -> errorResponseHandler.handleOperationInProgress(response)
            entry.input != existingEntry.input -> errorResponseHandler.handleOperationAlreadyPerformed(response)
            else -> response.writer.write(Base64.getDecoder().decode(existingEntry.output!!).decodeToString())
        }
    }

    fun updateResponse(entry: IdempotencyEntry, responseData: ByteArray) {
        entry.update(responseData = responseData)
        storage.update(entry)
    }

    fun remove(key: String) {
        storage.remove(key)
    }
}
