package com.carvalho.idempotency.configuration

import com.carvalho.idempotency.defaultimplementations.DefaultIdempotencyKeyGenerator
import com.carvalho.idempotency.IdempotencyFilter
import com.carvalho.idempotency.EntryRepository
import com.carvalho.idempotency.IdempotencyKeyGenerator
import com.carvalho.idempotency.IdempotentRequestHandler
import com.carvalho.idempotency.IdempotentResponseHandler
import com.carvalho.idempotency.defaultimplementations.LocalEntryRepository
import com.carvalho.idempotency.existingrequesthandlers.DuplicateRequest
import com.carvalho.idempotency.existingrequesthandlers.InProgressRequest
import com.carvalho.idempotency.existingrequesthandlers.InvalidIdempotencyKey
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.filter.OncePerRequestFilter

@Configuration
@EnableConfigurationProperties(RestAPIIdempotencyProperties::class)
@ConditionalOnProperty(prefix = "idempotency.enabled", value = ["true"], matchIfMissing = true)
class RestAPIIdempotencyAutoConfiguration(
    private val properties: RestAPIIdempotencyProperties
) {

    @Bean
    @ConditionalOnMissingBean(IdempotencyKeyGenerator::class)
    fun idempotencyKeyGeneratorBean() = DefaultIdempotencyKeyGenerator(properties)

    @Bean
    @ConditionalOnMissingBean(EntryRepository::class)
    fun idempotencyRepositoryBean() = LocalEntryRepository()

    @Bean
    fun idempotentRequestHandlerBean(keyGenerator: IdempotencyKeyGenerator, repository: EntryRepository) :IdempotentRequestHandler {
        val requestErrorHandlers = listOf(
            InvalidIdempotencyKey(properties),
            InProgressRequest(properties),
            DuplicateRequest(properties)
        )

        return IdempotentRequestHandler(keyGenerator, repository, requestErrorHandlers)
    }

    @Bean
    fun idempotentResponseHandlerBean(repository: EntryRepository) = IdempotentResponseHandler(repository)

    @Bean
    fun servletFilter(
        properties: RestAPIIdempotencyProperties,
        idempotentRequestHandler: IdempotentRequestHandler,
        idempotentResponseHandler: IdempotentResponseHandler
    ): OncePerRequestFilter = IdempotencyFilter(
        properties = properties,
        idempotentRequestHandler = idempotentRequestHandler,
        idempotentResponseHandler = idempotentResponseHandler
    )
}
