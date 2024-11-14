package com.github.jyc228.musinsa.domain.brand

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@DataJpaTest
class BrandRepositoryTest {

    @Autowired
    lateinit var em: TestEntityManager

    @Autowired
    lateinit var repository: BrandRepository

    @Test
    fun `deleteById`() {
        val brand = repository.save(BrandEntity(name = "test-1"))
        em.clear()

        repository.deleteById(brand.id)
        em.flush()
    }

    @Test
    fun `removeById`() {
        val brand = repository.save(BrandEntity(name = "test-2"))
        em.clear()

        repository.removeById(brand.id)
        em.flush()
    }

    @Test
    fun `addProductCount`() {
        val brand = repository.save(BrandEntity(name = "test-3"))
        em.clear()

        repository.addProductCount(brand.id, 1) shouldBe 1
        em.clear()
        repository.findByIdOrNull(brand.id)?.productCount shouldBe 1

        repository.addProductCount(brand.id, -5) shouldBe 1
        em.clear()
        repository.findByIdOrNull(brand.id)?.productCount shouldBe 0
    }
}
