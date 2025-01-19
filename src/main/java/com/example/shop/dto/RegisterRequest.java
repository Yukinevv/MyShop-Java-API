package com.example.shop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String username;
    private String password;
    private Boolean isAdmin;

    public Boolean isAdmin() {
        return isAdmin;
    }
}
