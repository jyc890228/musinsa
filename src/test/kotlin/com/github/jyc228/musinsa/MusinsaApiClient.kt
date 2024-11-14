package com.github.jyc228.musinsa

import com.github.jyc228.musinsa.domain.brand.BrandController
import com.github.jyc228.musinsa.domain.product.ProductController
import com.github.jyc228.musinsa.domain.statistics.StatisticsController
import io.kotest.assertions.throwables.shouldThrow
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLParameter
import io.ktor.http.isSuccess
import io.ktor.serialization.jackson.jackson
import java.math.BigInteger
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
        response.throwIfFail().body<BrandController.BrandIdResponse>().id
    }

    fun updateBrand(id: Long, name: String): Unit = runBlocking {
        http.put("/api/brands/$id") {
            setBody(BrandController.UpsertBrandRequest(name))
        }.throwIfFail()
    }

    fun deleteBrand(id: Long): Unit = runBlocking { http.delete("/api/brands/$id").throwIfFail() }

    fun createProduct(brandId: Long, categoryId: Int, price: BigInteger) = runBlocking {
        val response = http.post("/api/products") {
            setBody(ProductController.UpsertProductRequest(brandId, categoryId, price))
        }
        response.throwIfFail().body<ProductController.ProductIdResponse>().id
    }

    fun updateProduct(id: Long, brandId: Long, categoryId: Int, price: BigInteger): Unit = runBlocking {
        http.put("/api/products/$id") {
            setBody(ProductController.UpsertProductRequest(brandId, categoryId, price))
        }.throwIfFail()
    }

    fun deleteProduct(id: Long): Unit = runBlocking { http.delete("/api/products/$id").throwIfFail() }

    fun getCategoryProduct(categoryName: String): StatisticsController.CategoryProductResponse = runBlocking {
        http.get("/api/statistics/category-product/${categoryName.encodeURLParameter()}").throwIfFail().body()
    }

    fun getCategoryCheaperProduct(): StatisticsController.CategoryCheaperProductResponse = runBlocking {
        http.get("/api/statistics/category-cheaper-product").throwIfFail().body()
    }

    fun getBrandCheaperProduct(): StatisticsController.BrandCheaperProductResponse = runBlocking {
        http.get("/api/statistics/brand-cheaper-product").throwIfFail().body()
    }

    private suspend fun HttpResponse.throwIfFail(): HttpResponse {
        if (status.isSuccess()) return this
        throw ResponseException(status.value, body<MusinsaExceptionHandler.ErrorResponse>())
    }

    class ResponseException(
        val statusCode: Int,
        error: MusinsaExceptionHandler.ErrorResponse
    ) : RuntimeException("[$statusCode] ${error.message} ${error.value}")
}

fun shouldThrowResponseException(block: () -> Any?) = shouldThrow<MusinsaApiClient.ResponseException> {
    block()
}.apply { System.err.println(message) }