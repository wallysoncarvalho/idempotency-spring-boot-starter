package com.carvalho.idempotency

import com.carvalho.idempotency.configuration.RestAPIIdempotencyProperties
import com.carvalho.idempotency.defaultimplementations.DefaultIdempotencyKeyGenerator
import com.carvalho.idempotency.defaultimplementations.LocalEntryRepository
import com.carvalho.idempotency.existingrequesthandlers.DuplicateRequest
import com.carvalho.idempotency.existingrequesthandlers.InProgressRequest
import com.carvalho.idempotency.existingrequesthandlers.InvalidIdempotencyKey
import com.carvalho.idempotency.restapicomponents.ControllerAdvice
import com.carvalho.idempotency.restapicomponents.CounterController
import com.carvalho.idempotency.restapicomponents.Filter1
import com.carvalho.idempotency.restapicomponents.Filter2
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertNull

class IdempotencyFilterOnControllerTests {

    private val properties = RestAPIIdempotencyProperties().apply {
        enabled = true
        resources = listOf(
            RestAPIIdempotencyProperties.Resource("/increment", "POST")
        )
    }

    private val repository = LocalEntryRepository()

    private val idempotentRequestHandler = IdempotentRequestHandler(
        keyGenerator = DefaultIdempotencyKeyGenerator(properties),
        repository = repository,
        existingRequestHandlers = listOf(
            InProgressRequest(properties),
            InvalidIdempotencyKey(properties),
            DuplicateRequest(properties)
        )
    )

    private val idempotentResponseHandler = IdempotentResponseHandler(repository = repository)

    private val idempotencyFilter = IdempotencyFilter(
        properties = properties,
        idempotentRequestHandler = idempotentRequestHandler,
        idempotentResponseHandler = idempotentResponseHandler
    )

    private val mockMvc = MockMvcBuilders
        .standaloneSetup(CounterController())
        .setControllerAdvice(ControllerAdvice())
        .addFilters<StandaloneMockMvcBuilder>(idempotencyFilter)
        .build()

    @BeforeEach
    fun setUp() {
        CounterController.reset()
    }

    @Test
    fun `should call an idempotent resource`() {
        val repetitions = 5

        repeat(repetitions) {
            mockMvc.perform(
                post("/increment")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", "123")
                    .content("""{"message": "Hello"}""")
                    .characterEncoding("UTF-8")
            )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted)
                .andExpect(content().json("""{"message": "Cont changed 1."}"""))
        }

        assertEquals(1, CounterController.cont)
    }

    @Test
    fun `should call a non-idempotent resource`() {
        val repetitions = 5

        repeat(repetitions) {
            mockMvc.perform(
                post("/increment-without-idempotency")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"message": "Hello"}""")
            )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isAccepted)
                .andExpect(content().json("""{"message": "Cont changed."}"""))
        }

        assertEquals(repetitions, CounterController.contNonIdempotent)
    }

    @Test
    fun `should return error for same idempotency key with different request body`() {
        val idempotencyKey = UUID.randomUUID().toString()

        mockMvc.perform(
            post("/increment")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey)
                .content("""{"message": "Hello"}""")
                .characterEncoding("UTF-8")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isAccepted)
            .andExpect(content().json("""{"message": "Cont changed 1."}"""))

        mockMvc.perform(
            post("/increment")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey)
                .content("""{"message": "Request body changed"}""")
                .characterEncoding("UTF-8")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isConflict)
            .andExpect(content().json("""{"message": "Operation already performed"}"""))
    }

    @Test
    fun `should return error when IdempotencyKeyGenerator information is missing`() {
        mockMvc.perform(
            post("/increment")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"message": "Hello"}""")
                .characterEncoding("UTF-8")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest)
            .andExpect(content().json("""{"message": "Invalid key"}"""))
    }

    @Test
    fun `shouldn't persist request and response if controller finished with error`() {
        val idempotencyKey1 = UUID.randomUUID().toString()

        mockMvc.perform(
            post("/throws-error")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey1)
                .content("""{"message": "Hello"}""")
                .characterEncoding("UTF-8")
        ).andDo(MockMvcResultHandlers.print()).andExpect(status().is5xxServerError)

        assertNull(repository.getByKey("$idempotencyKey1:/throws-error:POST"))

        val idempotencyKey2 = UUID.randomUUID().toString()

        mockMvc.perform(
            post("/return-bad-request")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey2)
                .content("""{"message": "Hello"}""")
                .characterEncoding("UTF-8")
        ).andDo(MockMvcResultHandlers.print()).andExpect(status().isBadRequest)

        assertNull(repository.getByKey("$idempotencyKey2:/return-bad-request:POST"))
    }

    @Test
    fun `idempotency filter shouldn't impact other filters`() {
        val mockMvcMoreFilters = MockMvcBuilders
            .standaloneSetup(CounterController())
            .setControllerAdvice(ControllerAdvice())
            .addFilters<StandaloneMockMvcBuilder>(Filter1(), idempotencyFilter, Filter2())
            .build()

        val idempotencyKey = UUID.randomUUID().toString()

        // execute the request calling the controller method
        mockMvcMoreFilters.perform(
            post("/increment")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey)
                .content("""{"message": "Hello"}""")
                .characterEncoding("UTF-8")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isAccepted)
            .andExpect(content().json("""{"message": "Cont changed 1."}"""))
            .andExpect { result ->
                assertEquals("true", result.response.getHeader("Filter1"))
                assertEquals("true", result.response.getHeader("Filter2"))
            }

        // execute the same idempotent request expecting others filters changes to be returned
        mockMvcMoreFilters.perform(
            post("/increment")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", idempotencyKey)
                .content("""{"message": "Hello"}""")
                .characterEncoding("UTF-8")
        )
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isAccepted)
            .andExpect(content().json("""{"message": "Cont changed 1."}"""))
            .andExpect { result ->
                assertEquals("true", result.response.getHeader("Filter1"))
                assertEquals("true", result.response.getHeader("Filter2"))
            }
    }
}
