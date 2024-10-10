package com.demo.worker.worker.service;

import com.demo.worker.worker.model.Order;
import com.demo.worker.worker.model.OrderDocument;
import com.demo.worker.worker.model.ProductItem;
import com.demo.worker.worker.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class OrderConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(OrderConsumerService.class);
    private final OrderValidator orderValidator;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    public OrderConsumerService(OrderValidator orderValidator, OrderRepository orderRepository) {
        this.orderValidator = orderValidator;
        this.orderRepository = orderRepository;
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

                    return orderValidator.validateProducts(order.getProducts());
                })
                .flatMap(isProductsValid -> {
                    if (!isProductsValid) {
                        logger.warn("Productos inválidos o sin stock para el pedido: {}", order.getOrderId());
                        return Mono.empty();
                    }

                    OrderDocument orderDocument = new OrderDocument();
                    orderDocument.setOrderId(order.getOrderId());
                    orderDocument.setCustomerId(order.getClientId());
                    orderDocument.setCustomerName("Nombre del Cliente"); 
                    orderDocument.setProducts(order.getProducts());

                    return orderRepository.save(orderDocument)
                        .doOnSuccess(savedOrder -> logger.info("Pedido guardado correctamente con ID: {}", savedOrder.getOrderId()));
                })
                .doOnError(e -> logger.error("Error en la validación o procesamiento del pedido: {}", order.getOrderId(), e))
                .subscribe();

        } catch (Exception e) {
            logger.error("Error al parsear el mensaje: {}", message, e);
        }
    }
}
