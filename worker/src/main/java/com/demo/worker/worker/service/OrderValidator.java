package com.demo.worker.worker.service;

import com.demo.worker.worker.model.Client;
import com.demo.worker.worker.model.Product;
import com.demo.worker.worker.model.ProductItem;
import com.demo.worker.worker.model.Order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderValidator {
    private static final Logger logger = LoggerFactory.getLogger(OrderValidator.class);
    private final ApiClient apiClient;

    public OrderValidator(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public Mono<Client> validateClient(Order order) {
        String clientId = order.getClientId();
        return apiClient.makeRequestForMono("/clients/" + clientId, HttpMethod.GET, null, null, Client.class, order)
            .flatMap(client -> {
                if (client != null && client.isActive()) {
                    logger.info("Cliente {} es válido y activo.", clientId);
                    return Mono.just(client);
                } else {
                    logger.warn("Cliente {} no es válido o está inactivo.", clientId);
                    return Mono.empty(); 
                }
            })
            .doOnError(e -> logger.error("Error al validar el cliente: {}", clientId, e));
    }
    
    public Mono<List<Product>> validateProducts(Order order) {
        String productIds = order.getProducts().stream()
                .map(p -> "ids=" + p.getProductId())
                .collect(Collectors.joining("&"));
    
        return apiClient.makeRequestForFlux("/products/?" + productIds, HttpMethod.GET, null, null, Product.class, order)
                .collectList()
                .flatMap(products -> {
                    if (products == null || products.isEmpty()) {
                        logger.warn("No se encontraron productos para los IDs proporcionados: {}", productIds);
                        return Mono.empty();
                    }
    
                    boolean allProductsValid = order.getProducts().stream().allMatch(item -> {
                        Product product = products.stream()
                                .filter(p -> p.getId() == item.getProductId())
                                .findFirst()
                                .orElse(null);
    
                        return product != null && product.getStock() >= item.getQuantity();
                    });
    
                    if (!allProductsValid) {
                        logger.warn("Algunos productos no tienen suficiente stock o no existen.");
                        return Mono.empty();
                    }
    
                    logger.info("Todos los productos tienen suficiente stock para el pedido.");
                    return Mono.just(products);
                })
                .doOnError(e -> logger.error("Error al validar los productos: {}", productIds, e));
    }
    
}
