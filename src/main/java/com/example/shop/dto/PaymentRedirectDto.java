package com.example.shop.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Zwracamy userowi np. link do bramki płatności (lub token).
 */
@Getter
@Setter
public class PaymentRedirectDto {
    private String redirectUrl;

    public PaymentRedirectDto() {
    }

    public PaymentRedirectDto(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
