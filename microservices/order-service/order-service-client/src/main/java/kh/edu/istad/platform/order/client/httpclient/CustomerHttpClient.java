package kh.edu.istad.platform.order.client.httpclient;


import kh.edu.istad.platform.order.domain.dto.QueryCustomerResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@HttpExchange("/api/customers")
public interface CustomerHttpClient {

    @GetExchange
    Flux<QueryCustomerResult> queryCustomer() ;


    @GetExchange("/{id}")
    Mono<QueryCustomerResult> findCustomerById(@PathVariable UUID id);

}
