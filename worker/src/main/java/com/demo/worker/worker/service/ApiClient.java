package com.demo.worker.worker.service;

import com.demo.worker.worker.model.Product;
import com.demo.worker.worker.model.Order;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApiClient {

    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private final WebClient webClient;
    private final RedissonClient redissonClient;
    private static final String FAILED_MESSAGES_MAP = "failedApiCallsMap";
    private final ObjectMapper objectMapper;

    public ApiClient(WebClient.Builder webClientBuilder, RedissonClient redissonClient) {
        this.webClient = webClientBuilder.baseUrl("http://api:8000").build();
        this.redissonClient = redissonClient;
        this.objectMapper = new ObjectMapper();
    }

    public <T> Mono<T> makeRequestForMono(String url, HttpMethod method, Map<String, String> headers, Object body, Class<T> responseType, Order order) {
        return makeRequest(url, method, headers, body, responseType, Mono.class, order);
    }

    public <T> Flux<T> makeRequestForFlux(String url, HttpMethod method, Map<String, String> headers, Object body, Class<T> responseType, Order order) {
        return makeRequest(url, method, headers, body, responseType, Flux.class, order);
    }

    public Flux<Product> getProductsByIds(List<Integer> productIds) {
        String url = "/products?ids=" + productIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("&ids="));

        return makeRequestForFlux(url, HttpMethod.GET, null, null, Product.class, null)
                .doOnError(e -> logger.error("Error while retrieving products by IDs: ", e));
    }

    private <T, R> R makeRequest(String url, HttpMethod method, Map<String, String> headers, Object body, Class<T> responseType, Class<R> returnType, Order order) {
        WebClient.RequestBodySpec requestSpec = webClient.method(method).uri(url);

        if (headers != null && !headers.isEmpty()) {
            headers.forEach(requestSpec::header);
        }

        if (body != null) {
            requestSpec.contentType(MediaType.APPLICATION_JSON).bodyValue(body);
        }

        WebClient.ResponseSpec responseSpec = requestSpec.retrieve()
                .onStatus(
                    status -> !status.is2xxSuccessful(),
                    clientResponse -> clientResponse.createException()
                );

        RetryBackoffSpec retrySpec = Retry.backoff(3, Duration.ofSeconds(1))
                .maxBackoff(Duration.ofSeconds(10))
                .doAfterRetry(retrySignal -> {
                    int currentAttempt = (int) retrySignal.totalRetries() + 1;
                    logger.warn("Reintento {} para el pedido: {}", currentAttempt, order.getOrderId());
                    storeFailedMessage(order, currentAttempt);
                });

        if (returnType.equals(Mono.class)) {
            return (R) responseSpec.bodyToMono(responseType)
                    .retryWhen(retrySpec)
                    .doOnError(e -> logger.error("Error while making request: ", e));
        } else {
            return (R) responseSpec.bodyToFlux(responseType)
                    .retryWhen(retrySpec)
                    .doOnError(e -> logger.error("Error while making request: ", e));
        }
    }
    private void storeFailedMessage(Order order, int attempt) {
        RMap<String, String> failedMessagesMap = redissonClient.getMap(FAILED_MESSAGES_MAP);
        try {
            String serializedOrder = objectMapper.writeValueAsString(order);
            String attemptAsString = Integer.toString(attempt);
            failedMessagesMap.put(serializedOrder, attemptAsString); 
            logger.info("Mensaje fallido almacenado en Redis con contador de intentos: {}. Mensaje: {}", attempt, serializedOrder);
        } catch (JsonProcessingException e) {
            logger.error("Error al serializar el mensaje del pedido: {}", order.getOrderId(), e);
        }
    }
    
    
    
}
