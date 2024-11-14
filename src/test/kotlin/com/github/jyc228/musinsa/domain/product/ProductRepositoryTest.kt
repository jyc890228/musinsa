package com.github.jyc228.musinsa.domain.product

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    lateinit var repository: ProductRepository

    @Test
    fun `brandId, categoryId 제약 조건 테스트`() {
        shouldNotThrowAny { repository.save(ProductEntity(brandId = 1, categoryId = 1, price = 100.toBigInteger())) }
        shouldNotThrowAny { repository.save(ProductEntity(brandId = 1, categoryId = 2, price = 100.toBigInteger())) }
        shouldThrow<DataIntegrityViolationException> {
            repository.save(ProductEntity(brandId = 1, categoryId = 1, price = 100.toBigInteger()))
        }
    }
}
