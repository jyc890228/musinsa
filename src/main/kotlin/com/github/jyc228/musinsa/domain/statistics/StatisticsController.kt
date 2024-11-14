package com.github.jyc228.musinsa.domain.statistics

import com.github.jyc228.musinsa.StatisticsException
import com.github.jyc228.musinsa.domain.brand.BrandEntity
import com.github.jyc228.musinsa.domain.brand.BrandService
import com.github.jyc228.musinsa.domain.category.Category
import com.github.jyc228.musinsa.domain.product.ProductEntity
import java.math.BigInteger
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class StatisticsController(
    private val categoryStatisticsService: CategoryStatisticsService,
    private val brandStatisticsService: BrandStatisticsService,
    private val brandService: BrandService
) {

    @GetMapping("/api/statistics/category-product/{categoryName}")
    fun getCategoryProduct(@PathVariable categoryName: String): CategoryProductResponse {
        val (min, max) = categoryStatisticsService.getCategoryMinMaxProduct(Category.getOrThrow(categoryName).id)
            ?: throw StatisticsException("category $categoryName product not exist")
        val brandById = brandService.findAllById(setOf(min.brandId, max.brandId)).associateBy { it.id }
        return CategoryProductResponse(
            category = categoryName,
            lowestPrice = CategoryProductResponse.PriceData.of(min, brandById[min.brandId]),
            highestPrice = CategoryProductResponse.PriceData.of(max, brandById[max.brandId])
        )
    }


    @GetMapping("/api/statistics/category-cheaper-product")
    fun getCategoryCheaperProduct(): CategoryCheaperProductResponse {
        val products = categoryStatisticsService.getCategoryCheaperProduct()
        val brandById = brandService.findAllById(products.map { it.brandId }.toSet()).associateBy { it.id }

        return CategoryCheaperProductResponse(
            data = products.map {
                CategoryCheaperProductResponse.Element(
                    category = Category[it.categoryId]?.name ?: "Unknown Category",
                    brand = brandById[it.brandId]?.name ?: "Unknown Brand",
                    price = it.price,
                )
            },
            totalPrice = products.sumOf { it.price }
        )
    }

    @GetMapping("/api/statistics/brand-cheaper-product")
    fun getBrandCheaperProduct(): BrandCheaperProductResponse {
        val (brandId, products) = brandStatisticsService.getCheaperBrandProduct()
            ?: throw StatisticsException("cheaper brand products not exist")
        val brand = brandService.findByIdOrNull(brandId) ?: throw StatisticsException("brand $brandId not exist")
        return BrandCheaperProductResponse(
            lowestPrice = BrandCheaperProductResponse.LowestPriceData(
                brand = brand.name,
                category = products.map {
                    BrandCheaperProductResponse.Category(
                        category = Category[it.categoryId]?.name ?: "Unknown Category",
                        price = it.price,
                    )
                },
                totalPrice = products.sumOf { it.price }
            )
        )
    }

    private fun CategoryProductResponse.PriceData.Companion.of(
        product: ProductEntity,
        brand: BrandEntity?
    ) = CategoryProductResponse.PriceData(brand = brand?.name ?: "Unknown brand ", price = product.price)

    data class CategoryProductResponse(
        val category: String,
        val lowestPrice: PriceData,
        val highestPrice: PriceData,
    ) {
        data class PriceData(val brand: String, val price: BigInteger) {
            companion object
        }
    }

    data class CategoryCheaperProductResponse(
        val data: List<Element>,
        val totalPrice: BigInteger,
    ) {
        data class Element(val category: String, val brand: String, val price: BigInteger)
    }

    data class BrandCheaperProductResponse(val lowestPrice: LowestPriceData) {
        data class LowestPriceData(
            val brand: String,
            val category: List<Category>,
            val totalPrice: BigInteger
        )

        data class Category(val category: String, val price: BigInteger)
    }
}