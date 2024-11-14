package com.github.jyc228.musinsa.domain.product

sealed class ProductEvent {
    class Created(val entity: ProductEntity) : ProductEvent()
    class Updated(val prev: ProductEntity, val next: ProductEntity) : ProductEvent()
    class Deleted(val entity: ProductEntity) : ProductEvent()
}