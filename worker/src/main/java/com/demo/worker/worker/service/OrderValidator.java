package com.demo.worker.worker.service;

import com.demo.worker.worker.model.Client;
import com.demo.worker.worker.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors; 

@Service
public class OrderValidator {
    private static final Logger logger = LoggerFactory.getLogger(OrderValidator.class);
    private final ApiClient apiClient;

    public OrderValidator(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public Mono<Boolean> validateClient(String clientId) {
        return apiClient.makeRequestForMono("/clients/" + clientId, HttpMethod.GET, null, null, Client.class)
                .map(client -> {
                    if (client != null && client.isActive()) {
                        logger.info("Cliente {} es válido y activo.", clientId);
                        return true;
                    } else {
                        logger.warn("Cliente {} no es válido o está inactivo.", clientId);
                        return false;
                    }
                })
                .defaultIfEmpty(false)
                .doOnError(e -> logger.error("Error al validar el cliente: {}", clientId, e));
    }

    public Mono<Boolean> validateProducts(List<Integer> productIds) {
        String ids = productIds.stream().map(String::valueOf).collect(Collectors.joining("&ids="));
        String url = "/products/?ids=" + ids;
    
        return apiClient.makeRequestForFlux(url, HttpMethod.GET, null, null, Product.class)
                .collectList()
                .map(products -> {
                    if (products == null || products.isEmpty()) {
                        logger.warn("No se encontraron productos para los IDs: {}", productIds);
                        return false;
                    }
    
                    boolean allInStock = products.stream().allMatch(product -> product.getStock() > 0);
                    if (allInStock) {
                        logger.info("Todos los productos {} tienen stock disponible.", productIds);
                    } else {
                        logger.warn("Algunos productos no tienen stock suficiente para los IDs: {}", productIds);
                    }
                    return allInStock;
                })
                .defaultIfEmpty(false)
                .doOnError(e -> logger.error("Error al validar los productos: {}", productIds, e));
    }
    
}
