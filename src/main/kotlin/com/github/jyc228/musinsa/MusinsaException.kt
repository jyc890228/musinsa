package com.github.jyc228.musinsa

sealed class MusinsaException(
    message: String? = null,
    cause: MusinsaException? = null
) : RuntimeException(message, cause) {
    open val value: Any? = cause?.value
}

/** @param value 브랜드 id */
class BrandNotFoundException(override val value: Long) : MusinsaException()

/** @param value 카테고리 이름 or id */
class CategoryNotFoundException(override val value: Any) : MusinsaException()

/** @param value 상품 id */
class ProductNotFoundException(override val value: Long) : MusinsaException()

class InvalidRequestException(override val value: Any, message: String) : MusinsaException(message)

class StatisticsException(override val message: String, override val value: Any? = null) : MusinsaException(message)

class UpsertProductException(override val cause: MusinsaException) : MusinsaException(cause = cause)