package com.demo.worker.worker.service;

import com.demo.worker.worker.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
public class OrderConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(OrderConsumerService.class);
    private final OrderValidator orderValidator;
    private final ObjectMapper objectMapper;

    public OrderConsumerService(OrderValidator orderValidator) {
        this.orderValidator = orderValidator;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "orders", groupId = "order-group")
    public void consume(String message) {
        logger.info("Mensaje recibido del tópico: {}", message);

        try {
            Order order = objectMapper.readValue(message, Order.class);

            if (order.getClientId() == null || order.getProducts() == null || order.getProducts().isEmpty()) {
                logger.error("Datos del pedido inválidos: clientId o productos faltantes");
                return;
            }

            orderValidator.validateClient(order.getClientId())
                .flatMap(isClientValid -> {
                    if (!isClientValid) {
                        logger.warn("Cliente no encontrado o inactivo para el pedido: {}", order.getOrderId());
                        return Mono.empty(); 
                    }

                    return orderValidator.validateProducts(
                            order.getProducts().stream().map(p -> p.getProductId()).collect(Collectors.toList())
                    );
                })
                .flatMap(areProductsValid -> {
                    if (!areProductsValid) {
                        logger.warn("Productos inválidos para el pedido: {}", order.getOrderId());
                        return Mono.empty(); 
                    }

                    logger.info("Pedido válido procesado: {}", order.getOrderId());
                    return Mono.just(order);
                })
                .doOnError(e -> logger.error("Error en la validación del pedido: {}", order.getOrderId(), e))
                .subscribe();

        } catch (Exception e) {
            logger.error("Error al parsear el mensaje: {}", message, e);
        }
    }
}
