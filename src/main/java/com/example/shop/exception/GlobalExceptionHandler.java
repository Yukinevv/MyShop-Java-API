package com.example.shop.exception;

import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.OptimisticLockException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // np. obsługa "zwykłych" RuntimeException
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ErrorResponse errorResponse = new ErrorResponse("RUNTIME_ERROR", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // obsługa, gdy użytkownik nie znaleziony
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UsernameNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse("USER_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    // obsługa błędów JWT (np. token wygasł, jest niepoprawny)
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(JwtException ex) {
        ErrorResponse errorResponse = new ErrorResponse("INVALID_TOKEN", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {

        // Obiekt, w którym przechowasz listę błędów
        ValidationErrorResponse errorResponse = new ValidationErrorResponse("VALIDATION_ERROR");

        // Wyciągam szczegóły o błędach z bindingResult
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            // np. "username" -> "Username nie może być pusty"
            String fieldName = fieldError.getField();
            String message = fieldError.getDefaultMessage();
            errorResponse.addError(fieldName, message);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockException ex) {
        ErrorResponse errorResponse = new ErrorResponse("OPTIMISTIC_LOCK_ERROR", "Produkt został zmodyfikowany w międzyczasie. Spróbuj ponownie.");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }
}
