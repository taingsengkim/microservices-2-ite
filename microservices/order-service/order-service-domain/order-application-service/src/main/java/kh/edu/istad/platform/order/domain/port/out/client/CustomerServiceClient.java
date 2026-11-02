package kh.edu.istad.platform.order.domain.port.out.client;

import kh.edu.istad.platform.order.domain.dto.QueryCustomerResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CustomerServiceClient {

    Flux<QueryCustomerResult> getCustomerNames();
    Mono<QueryCustomerResult> getCustomerById(UUID uuid);
}
