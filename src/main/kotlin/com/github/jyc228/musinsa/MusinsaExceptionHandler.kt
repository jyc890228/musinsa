package com.github.jyc228.musinsa

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class MusinsaExceptionHandler {

    @ExceptionHandler(MusinsaException::class)
    fun exceptionHandler(e: MusinsaException): ResponseEntity<ErrorResponse> = when (e) {
        is BrandNotFoundException -> HttpStatus.NOT_FOUND body "brand not found"
        is CategoryNotFoundException -> HttpStatus.NOT_FOUND body "category not found"
        is InvalidRequestException -> HttpStatus.BAD_REQUEST body "invalid parameter ${e.param}"
        is UpsertProductException -> HttpStatus.BAD_REQUEST body "create product failed. reason: ${exceptionHandler(e.cause).body?.message}"
    }

    data class ErrorResponse(val message: String)

    private infix fun HttpStatus.body(message: String) = ResponseEntity.status(this).body(ErrorResponse(message))
}