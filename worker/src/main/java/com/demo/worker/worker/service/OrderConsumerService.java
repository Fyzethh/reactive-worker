package com.demo.worker.worker.service;

import com.demo.worker.worker.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OrderConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(OrderConsumerService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "orders", groupId = "order-group")
    public void consume(String message) {
        try {
            Order order = objectMapper.readValue(message, Order.class);
            logger.info("Pedido recibido: ID del pedido = {}, ID del cliente = {}, Productos = {}",
                    order.getOrderId(), order.getClientId(), order.getProducts());

            // TODO: process order here

        } catch (Exception e) {
            logger.error("Error procesando el mensaje: ", e);
        }
    }
}
