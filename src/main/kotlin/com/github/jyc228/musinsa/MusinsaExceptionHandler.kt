package com.github.jyc228.musinsa

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class MusinsaExceptionHandler {

    @ExceptionHandler(MusinsaException::class)
    fun exceptionHandler(e: MusinsaException): ResponseEntity<ErrorResponse> = when (e) {
        is BrandNotFoundException -> HttpStatus.NOT_FOUND body ("brand not found" withValue e)
        is CategoryNotFoundException -> HttpStatus.NOT_FOUND body ("category not found" withValue e)
        is ProductNotFoundException -> HttpStatus.NOT_FOUND body ("product not found" withValue e)
        is InvalidRequestException -> HttpStatus.BAD_REQUEST body ("invalid parameter [${e.message}]" withValue e)
        is UpsertProductException -> HttpStatus.BAD_REQUEST body ("create product failed. ${exceptionHandler(e.cause).body?.message}" withValue e)
        is StatisticsException -> HttpStatus.INTERNAL_SERVER_ERROR body ("internal server error" withValue e)
    }

    data class ErrorResponse(val value: Any?, val message: String)

    private infix fun String.withValue(e: MusinsaException) = ErrorResponse(e.value, this)

    private infix fun HttpStatus.body(error: ErrorResponse) = ResponseEntity.status(this).body(error)
}