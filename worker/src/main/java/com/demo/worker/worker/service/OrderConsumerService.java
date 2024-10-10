package com.demo.worker.worker.service;

import com.demo.worker.worker.model.Order;
import com.demo.worker.worker.model.OrderDocument;
import com.demo.worker.worker.model.ProductItem;
import com.demo.worker.worker.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class OrderConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(OrderConsumerService.class);
    private final OrderValidator orderValidator;
    private final OrderRepository orderRepository;
    private final RedissonClient redissonClient;
    private final ApiClient apiClient;
    private final ObjectMapper objectMapper;

    public OrderConsumerService(OrderValidator orderValidator, OrderRepository orderRepository, RedissonClient redissonClient, ApiClient apiClient) {
        this.orderValidator = orderValidator;
        this.orderRepository = orderRepository;
        this.redissonClient = redissonClient;
        this.apiClient = apiClient;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "orders", groupId = "order-group")
    public void consume(String message) {
        logger.info("Mensaje recibido del tópico: {}", message);
    
        try {
            Order order = objectMapper.readValue(message, Order.class);
            order.setOriginalMessage(message);
    
            if (order.getClientId() == null || order.getProducts() == null || order.getProducts().isEmpty()) {
                logger.error("Datos del pedido inválidos: clientId o productos faltantes");
                return;
            }
    
            String lockKey = "client-lock-" + order.getClientId();
            RLock lock = redissonClient.getLock(lockKey);
    
            boolean isLocked = false;
            try {
                isLocked = lock.tryLock(10, 30, TimeUnit.SECONDS);
    
                if (isLocked) {
                    orderValidator.validateClient(order)
                        .flatMap(client -> {
                            if (client == null) {
                                logger.warn("Cliente no encontrado o inactivo para el pedido: {}", order.getOrderId());
                                return Mono.empty();
                            }
                            return orderValidator.validateProducts(order)
                                .flatMap(products -> {
                                    if (products.isEmpty()) {
                                        logger.warn("Productos inválidos o sin stock para el pedido: {}", order.getOrderId());
                                        return Mono.empty();
                                    }
    
                                    OrderDocument orderDocument = new OrderDocument();
                                    orderDocument.setOrderId(order.getOrderId());
                                    orderDocument.setCustomerId(order.getClientId());
                                    orderDocument.setCustomerName(client.getName());
                                    orderDocument.setProducts(order.getProducts());
    
                                    return orderRepository.save(orderDocument)
                                        .flatMap(savedOrder -> {
                                            logger.info("Pedido guardado correctamente con ID: {}", savedOrder.getOrderId());
    
                                            List<Map<String, Integer>> stockUpdates = order.getProducts().stream()
                                                .map(product -> Map.of(
                                                    "product_id", product.getProductId(),
                                                    "quantity", product.getQuantity()
                                                ))
                                                .collect(Collectors.toList());
    
                                            return apiClient.makeRequestForMono("/products/stock", HttpMethod.PATCH, null, stockUpdates, Void.class, order)
                                                .flatMap(response -> {
                                                    logger.info("Stock actualizado correctamente para el pedido: {}", order.getOrderId());
                                                    return Mono.just(savedOrder);
                                                })
                                                .onErrorResume(e -> {
                                                    logger.error("Error al actualizar el stock de productos: {}", e.getMessage());
                                                    return Mono.empty();
                                                });
                                        });
                                });
                        })
                        .doOnError(e -> logger.error("Error en la validación o procesamiento del pedido: {}", order.getOrderId(), e))
                        .subscribe();
                } else {
                    logger.warn("No se pudo adquirir el lock para el cliente: {}. Otro proceso ya está manejando este pedido.", order.getClientId());
                }
            } catch (InterruptedException e) {
                logger.error("Error al intentar adquirir el lock para el cliente: {}", order.getClientId(), e);
                Thread.currentThread().interrupt();
            } finally {
                if (isLocked) {
                    lock.unlock();
                }
            }
    
        } catch (Exception e) {
            logger.error("Error al parsear el mensaje: {}", message, e);
        }
    }
    
}
