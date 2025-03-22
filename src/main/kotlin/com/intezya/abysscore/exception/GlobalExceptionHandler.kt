package com.intezya.abysscore.exception

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.NoHandlerFoundException
import java.time.LocalDateTime

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    data class ApiError(
        val timestamp: String =
            java.time.OffsetDateTime
                .now()
                .toString(),
        val status: Int,
        val error: String,
        val message: String,
        val path: String,
        val details: Map<String, List<String>>? = null,
    )

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> {
        val errors = ex.bindingResult.fieldErrors.groupBy({ it.field }, { it.defaultMessage ?: "Invalid value" })
        val apiError =
            ApiError(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Validation Failed",
                message = "Invalid request parameters",
                path = request.requestURI,
                details = errors,
            )

        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadable(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> {
        val errors = ex.cause?.message?.let { mapOf("message" to listOf(it)) } ?: emptyMap()
        println(errors)
        val apiError =
            ApiError(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = "Malformed JSON request",
                path = request.requestURI,
                details = errors,
            )

        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        ex: ConstraintViolationException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> {
        val errors = ex.constraintViolations.groupBy({ it.propertyPath.last().name }, { it.message })

        val apiError =
            ApiError(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Validation Failed",
                message = "Constraint violations",
                path = request.requestURI,
                details = errors,
            )

        return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(
        ex: BadCredentialsException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> {
        val apiError =
            ApiError(
                status = HttpStatus.UNAUTHORIZED.value(),
                error = "Unauthorized",
                message = ex.message ?: "Invalid credentials",
                path = request.requestURI,
            )

        return ResponseEntity(apiError, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(
        ex: ResponseStatusException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiError> {
        val apiError =
            ApiError(
                status = ex.statusCode.value(),
                error = ex.statusCode.toString(),
                message = ex.reason ?: "No message available",
                path = request.requestURI,
            )

        return ResponseEntity(apiError, ex.statusCode)
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(request: HttpServletRequest): ResponseEntity<ApiError> {
        val apiError =
            ApiError(
                status = HttpStatus.NOT_FOUND.value(),
                error = "Not Found",
                message = "No matching route found for ${request.method} ${request.requestURI}",
                path = request.requestURI,
            )

        return ResponseEntity(apiError, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        ex.printStackTrace()
        logger.error(ex.message, ex.cause)
        val errorResponse =
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                message = "An unexpected error occurred",
                timestamp = LocalDateTime.now(),
            )
        return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
