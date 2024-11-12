package com.github.jyc228.musinsa

import com.github.jyc228.musinsa.domain.brand.BrandController
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.runBlocking

class MusinsaApiClient(url: String) {
    private val http = HttpClient {
        defaultRequest {
            url(url)
            contentType(ContentType.Application.Json)
        }
        install(ContentNegotiation) { jackson { } }
    }

    fun createBrand(name: String): Long = runBlocking {
        val response = http.post("/api/brands") {
            setBody(BrandController.UpsertBrandRequest(name))
        }
        response.body<BrandController.BrandIdResponse>().id
    }

    fun updateBrand(id: Long, name: String) = runBlocking {
        val response = http.put("/api/brands/$id") {
            setBody(BrandController.UpsertBrandRequest(name))
        }
        if (response.status.isSuccess()) return@runBlocking
        throw ResponseException(response.status.value, response.body<MusinsaExceptionHandler.ErrorResponse>().message)
    }

    class ResponseException(statusCode: Int, message: String) : RuntimeException("[$statusCode] $message")
}