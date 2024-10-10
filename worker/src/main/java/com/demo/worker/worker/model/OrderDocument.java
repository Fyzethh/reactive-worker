package com.demo.worker.worker.model;

import java.util.List;

public class OrderDocument {
    private String orderId;
    private String customerId;
    private String customerName;
    private List<ProductItem> products;


    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public List<ProductItem> getProducts() {
        return products;
    }


    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setProducts(List<ProductItem> products) {
        this.products = products;
    }
}
