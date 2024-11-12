package com.github.jyc228.musinsa.domain.brand

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class BrandController(private val service: BrandService) {

    @PostMapping("/api/brands")
    fun createBrand(@RequestBody request: UpsertBrandRequest): BrandIdResponse {
        return BrandIdResponse(service.createBrand(request).id)
    }

    data class UpsertBrandRequest(val name: String)

    data class BrandIdResponse(val id: Long)
}