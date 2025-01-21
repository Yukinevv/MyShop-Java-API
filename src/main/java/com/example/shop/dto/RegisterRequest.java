package com.example.shop.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private Boolean isAdmin;

    public Boolean isAdmin() {
        return isAdmin;
    }
}
