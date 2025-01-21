package com.example.shop.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ValidationErrorResponse {
    private String errorCode; // np. "VALIDATION_ERROR"
    private List<FieldErrorDTO> errors = new ArrayList<>();

    public ValidationErrorResponse(String errorCode) {
        this.errorCode = errorCode;
    }

    public void addError(String field, String message) {
        this.errors.add(new FieldErrorDTO(field, message));
    }

    @Getter
    @Setter
    public static class FieldErrorDTO {
        private String field;
        private String message;

        public FieldErrorDTO(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
}
