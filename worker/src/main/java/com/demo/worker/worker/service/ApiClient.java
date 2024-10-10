package com.demo.worker.worker.service;

import com.demo.worker.worker.model.Product;
import com.demo.worker.worker.model.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApiClient {

    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private final WebClient webClient;

    public ApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://api:8000").build();
    }

    public <T> Mono<T> makeRequestForMono(String url, HttpMethod method, Map<String, String> headers, Object body, Class<T> responseType) {
        return makeRequest(url, method, headers, body, responseType, Mono.class);
    }

    public <T> Flux<T> makeRequestForFlux(String url, HttpMethod method, Map<String, String> headers, Object body, Class<T> responseType) {
        return makeRequest(url, method, headers, body, responseType, Flux.class);
    }

    public Flux<Product> getProductsByIds(List<Integer> productIds) {
        String url = "/products?ids=" + productIds.stream()
                                                 .map(String::valueOf)
                                                 .collect(Collectors.joining("&ids="));

        return makeRequestForFlux(url, HttpMethod.GET, null, null, Product.class)
                .doOnError(e -> logger.error("Error while retrieving products by IDs: ", e));
    }

    private <T, R> R makeRequest(String url, HttpMethod method, Map<String, String> headers, Object body, Class<T> responseType, Class<R> returnType) {
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

        if (returnType.equals(Mono.class)) {
            return (R) responseSpec.bodyToMono(responseType)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)).maxBackoff(Duration.ofSeconds(10)))
                    .doOnError(e -> logger.error("Error while making request: ", e));
        } else {
            return (R) responseSpec.bodyToFlux(responseType)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)).maxBackoff(Duration.ofSeconds(10)))
                    .doOnError(e -> logger.error("Error while making request: ", e));
        }
    }
}
