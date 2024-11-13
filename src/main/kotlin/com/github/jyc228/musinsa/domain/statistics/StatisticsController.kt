package com.github.jyc228.musinsa.domain.statistics

import com.github.jyc228.musinsa.domain.brand.BrandService
import com.github.jyc228.musinsa.domain.category.CategoryService
import java.math.BigInteger
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class StatisticsController(
    private val categoryStatisticsService: CategoryStatisticsService,
    private val categoryService: CategoryService,
    private val brandService: BrandService
) {

    @GetMapping("/api/statistics/category-cheaper-product")
    fun getCategoryCheaperProduct(): CategoryCheaperProductResponse {
        val products = categoryStatisticsService.getCategoryCheaperProduct()
        val brandById = brandService.findAllById(products.map { it.brandId }.toSet()).associateBy { it.id }
        return CategoryCheaperProductResponse(
            data = products.map {
                CategoryCheaperProductResponse.Element(
                    category = categoryService.getByIdOrNull(it.categoryId)?.name ?: "Unknown Category",
                    brand = brandById[it.brandId]?.name ?: "Unknown Brand",
                    price = it.price,
                )
            },
            totalPrice = products.sumOf { it.price }
        )
    }

    data class CategoryCheaperProductResponse(
        val data: List<Element>,
        val totalPrice: BigInteger,
    ) {
        data class Element(val category: String, val brand: String, val price: BigInteger)
    }
}