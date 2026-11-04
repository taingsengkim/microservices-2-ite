package kh.edu.istad.platform.order.client.adapter;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import kh.edu.istad.platform.grpc.customer.v1.*;
import kh.edu.istad.platform.order.domain.dto.QueryCustomerResult;
import kh.edu.istad.platform.order.domain.port.out.client.CustomerServiceClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "customer.transport", havingValue = "grpc")
public class CustomerGrpcClientAdapter implements CustomerServiceClient, AutoCloseable {
    private final ManagedChannel channel;
    private final CustomerServiceGrpc.CustomerServiceStub stub;
    public CustomerGrpcClientAdapter(@Value("${customer.grpc.target:localhost:9093}") String target) {
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = CustomerServiceGrpc.newStub(channel);
    }
    @Override
    public Flux<QueryCustomerResult> getCustomerNames() {
        return Flux.create(sink -> stub.withDeadlineAfter(5, TimeUnit.SECONDS)
            .listCustomers(ListCustomersRequest.getDefaultInstance(), observer(sink)));
    }
    @Override
    public Mono<QueryCustomerResult> getCustomerById(UUID id) {
        return Flux.<QueryCustomerResult>create(sink -> stub.withDeadlineAfter(5, TimeUnit.SECONDS)
            .getCustomer(GetCustomerRequest.newBuilder().setId(id.toString()).build(), observer(sink))).single();
    }
    private static <T> ClientResponseObserver<T, CustomerReply> observer(FluxSink<QueryCustomerResult> sink) {
        return new ClientResponseObserver<>() {
            public void beforeStart(ClientCallStreamObserver<T> call) {
                sink.onCancel(() -> call.cancel("HTTP subscriber cancelled", null));
            }
            public void onNext(CustomerReply reply) { sink.next(new QueryCustomerResult(reply.getFamilyName())); }
            public void onError(Throwable error) { sink.error(error); }
            public void onCompleted() { sink.complete(); }
        };
    }
    @Override
    public void close() { channel.shutdownNow(); }
}
