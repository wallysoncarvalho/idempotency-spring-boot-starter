package com.carvalho.springidempotencyapi

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/remove-me")
class RemoveMeController {

    @PostMapping
    fun sdasdsad(@RequestBody reqBodyTest: ReqBodyTest): String {

        return "Passou! $reqBodyTest"
    }

    @GetMapping("/get/{id}")
    fun get(@PathVariable id: String): String {
        return "Passou! $id"
    }
}

data class ReqBodyTest(val name: String, val age: Int)
