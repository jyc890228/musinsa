package com.github.jyc228.musinsa.domain.product

import com.github.jyc228.musinsa.InvalidRequestException
import com.github.jyc228.musinsa.MusinsaException
import com.github.jyc228.musinsa.ProductNotFoundException
import com.github.jyc228.musinsa.UpsertProductException
import com.github.jyc228.musinsa.domain.category.Category
import java.math.BigInteger
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ProductController(private val service: ProductService) {
    @PostMapping("/api/products")
    fun createProduct(@RequestBody request: UpsertProductRequest): ProductIdResponse {
        request.throwIfInvalid()
        return try {
            ProductIdResponse(service.createProduct(request).id)
        } catch (e: MusinsaException) {
            throw UpsertProductException(e)
        }
    }

    @PutMapping("/api/products/{productId}")
    fun updateProduct(
        @PathVariable productId: Long,
        @RequestBody request: UpsertProductRequest
    ) {
        request.throwIfInvalid()
        try {
            service.updateProduct(request, productId)
        } catch (e: MusinsaException) {
            if (e is ProductNotFoundException) throw e
            throw UpsertProductException(e)
        }
    }

    @DeleteMapping("/api/products/{productId}")
    fun deleteProduct(@PathVariable productId: Long) {
        service.deleteProduct(productId)
    }

    data class UpsertProductRequest(
        val brandId: Long,
        val categoryId: Int,
        val price: BigInteger
    ) {
        fun throwIfInvalid() {
            if (price <= BigInteger.ZERO) throw InvalidRequestException(price, "invalid price")
            Category.throwIfNotExist(categoryId)
        }
    }

    data class ProductIdResponse(val id: Long)
}