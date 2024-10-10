package com.demo.worker.worker.model;

import java.util.List;

public class ProductListResponse {
    private List<Product> products;

    public ProductListResponse(List<Product> products) {
        this.products = products;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
