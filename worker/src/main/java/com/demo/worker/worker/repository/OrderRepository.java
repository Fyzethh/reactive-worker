package com.demo.worker.worker.repository;

import com.demo.worker.worker.model.OrderDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends ReactiveMongoRepository<OrderDocument, String> {
}
