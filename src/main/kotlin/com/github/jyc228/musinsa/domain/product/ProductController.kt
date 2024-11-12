package com.github.jyc228.musinsa.domain.product

import com.github.jyc228.musinsa.MusinsaException
import com.github.jyc228.musinsa.UpsertProductException
import java.math.BigInteger
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductController(private val service: ProductService) {
    @PostMapping("/api/products")
    fun createProduct(@RequestBody request: UpsertProductRequest): ProductIdResponse {
        return try {
            ProductIdResponse(service.createProduct(request).id)
        } catch (e: MusinsaException) {
            throw UpsertProductException(e)
        }
    }

    data class UpsertProductRequest(
        val brandId: Long,
        val categoryId: Long,
        val price: BigInteger
    )

    data class ProductIdResponse(val id: Long)
}