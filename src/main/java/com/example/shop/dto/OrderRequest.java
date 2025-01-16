package com.example.shop.dto;

import java.util.List;

public class OrderRequest {
    private List<Long> productIds;

    // gettery, settery
    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }
}
