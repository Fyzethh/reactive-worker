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
        WebClient.RequestBodySpec requestSpec = webClient.method(method).uri(url);

        if (headers != null && !headers.isEmpty()) {
            headers.forEach(requestSpec::header);
        }

        if (body != null) {
            requestSpec.contentType(MediaType.APPLICATION_JSON).bodyValue(body);
        }

        return requestSpec.retrieve()
                .bodyToMono(responseType)
                .doOnError(e -> logger.error("Error while making request: ", e));
    }

    public Flux<Product> getProductsByIds(List<Integer> productIds) {
        String url = "/products?ids=" + productIds.stream()
                                                 .map(String::valueOf)
                                                 .collect(Collectors.joining("&ids="));

        return webClient.get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Product.class)
                .doOnError(e -> logger.error("Error while making request: ", e));
    }
}
