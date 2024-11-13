package com.github.jyc228.musinsa.domain.statistics

import com.github.jyc228.musinsa.domain.product.ProductEntity
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@DataJpaTest
class StatisticsDatabaseTest {
    @Autowired
    lateinit var em: TestEntityManager

    @Test
    fun `카테고리별 가장 싼 가격 쿼리 테스트`() {
        listOf(
            ProductEntity(brandId = 1, categoryId = 1, price = 1000.toBigInteger()),
            ProductEntity(brandId = 1, categoryId = 2, price = 5000.toBigInteger()),
            ProductEntity(brandId = 2, categoryId = 2, price = 15000.toBigInteger()),
            ProductEntity(brandId = 1, categoryId = 3, price = 300.toBigInteger()),
            ProductEntity(brandId = 2, categoryId = 3, price = 300.toBigInteger()),
            ProductEntity(brandId = 3, categoryId = 3, price = 15000.toBigInteger()),
        ).forEach { em.persist(it) }

        val db = StatisticsDatabase(em.entityManager)
        db.findCheaperProductByCategoryId(1)?.price shouldBe 1000.toBigInteger()
        db.findCheaperProductByCategoryId(2)?.price shouldBe 5000.toBigInteger()
        db.findCheaperProductByCategoryId(3)?.price shouldBe 300.toBigInteger()
    }
}