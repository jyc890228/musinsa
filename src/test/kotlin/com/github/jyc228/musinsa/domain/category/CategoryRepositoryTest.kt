package com.github.jyc228.musinsa.domain.category

import io.kotest.matchers.collections.shouldHaveAtLeastSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    lateinit var repository: CategoryRepository

    @Test
    fun `초기화 스크립트로 적재된 데이터 확인`() {
        repository.findAll() shouldHaveAtLeastSize 1
    }
}
