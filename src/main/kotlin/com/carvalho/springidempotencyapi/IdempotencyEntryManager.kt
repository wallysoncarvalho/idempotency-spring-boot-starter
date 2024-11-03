package com.carvalho.springidempotencyapi

import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service

@Service
class IdempotencyEntryManager(
    private val storage: IdempotencyAPIRepository,
    private val errorResponseHandler: ErrorResponseHandler
) {

    fun trySave(entry: IdempotencyEntry, response: HttpServletResponse): Boolean = runCatching {
        storage.save(entry)
    }.onFailure {
        if (it !is LocalIdempotencyAPIRepository.DuplicateStorageKeyException) throw it
        handleDuplicateKey(entry, response)
        return false
    }.isSuccess

    private fun handleDuplicateKey(entry: IdempotencyEntry, response: HttpServletResponse) {
        val existingEntry = storage.getByKey(entry.key)!!

        when {
            existingEntry.response == null -> errorResponseHandler.handleOperationInProgress(response)
            !entry.request.contentEquals(existingEntry.request) -> errorResponseHandler.handleOperationAlreadyPerformed(response)
            else -> response.writer.write(existingEntry.response!!.decodeToString())
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
