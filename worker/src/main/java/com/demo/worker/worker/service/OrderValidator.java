package com.demo.worker.worker.service;

import com.demo.worker.worker.model.Client;
import com.demo.worker.worker.model.Product;
import com.demo.worker.worker.model.ProductItem;
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

    public Mono<Boolean> validateProducts(List<ProductItem> productItems) {
        String productIds = productItems.stream()
                .map(p -> "ids=" + p.getProductId())
                .collect(Collectors.joining("&"));
        
        return apiClient.makeRequestForFlux("/products/?" + productIds, HttpMethod.GET, null, null, Product.class)
                .collectList()
                .map(products -> {
                    if (products == null || products.isEmpty()) {
                        logger.warn("No se encontraron productos para los IDs proporcionados: {}", productIds);
                        return false;
                    }
    
                    for (ProductItem item : productItems) {
                        Product product = products.stream()
                                .filter(p -> p.getId() == item.getProductId())
                                .findFirst()
                                .orElse(null);
    
                        if (product == null || product.getStock() < item.getQuantity()) {
                            logger.warn("Producto con ID {} no tiene suficiente stock o no existe.", item.getProductId());
                            return false;
                        }
                    }
    
                    logger.info("Todos los productos tienen suficiente stock para el pedido.");
                    return true;
                })
                .defaultIfEmpty(false)
                .doOnError(e -> logger.error("Error al validar los productos: {}", productIds, e));
    }
    
}
