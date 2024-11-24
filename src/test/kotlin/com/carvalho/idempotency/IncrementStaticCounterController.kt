package com.carvalho.idempotency

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping
class IncrementStaticCounterController {

    @PostMapping("/increment")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun increment(@RequestBody request: Request): Response{
        cont++

        return Response("Cont changed $cont.")
    }

    @PostMapping("/increment-without-idempotency")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun incrementWithoutIdempotency(): Response {
        contNonIdempotent++

        return Response("Cont changed.")
    }

    @PostMapping("/throws-error")
    fun throwsError(){
        throw RuntimeException("Error")
    }

    @PostMapping("/return-bad-request")
    fun returnBadRequest(): ResponseEntity<String>{
        return ResponseEntity.badRequest().body("Bad Request")
    }

    companion object{
        var cont = 0
        var contNonIdempotent = 0

        fun reset(){
            cont = 0
            contNonIdempotent = 0
        }
    }
}

data class Request(val message: String)

data class Response(val message: String)
