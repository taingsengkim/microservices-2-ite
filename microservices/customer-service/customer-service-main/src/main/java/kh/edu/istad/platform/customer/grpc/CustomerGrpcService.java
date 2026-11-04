package kh.edu.istad.platform.customer.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.grpc.stub.ServerCallStreamObserver;
import kh.edu.istad.platform.grpc.customer.v1.*;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.usecase.QueryCustomerUseCase;
import org.springframework.stereotype.Component;
import reactor.core.Disposables;
import java.util.UUID;

@Component
public class CustomerGrpcService extends CustomerServiceGrpc.CustomerServiceImplBase {
    private final QueryCustomerUseCase customers;
    public CustomerGrpcService(QueryCustomerUseCase customers) { this.customers = customers; }

    @Override
    public void getCustomer(GetCustomerRequest request, StreamObserver<CustomerReply> observer) {
        UUID id;
        try { id = UUID.fromString(request.getId()); }
        catch (IllegalArgumentException e) {
            observer.onError(Status.INVALID_ARGUMENT.withDescription("id must be a UUID").asRuntimeException());
            return;
        }
        var subscription = Disposables.swap();
        ((ServerCallStreamObserver<CustomerReply>) observer).setOnCancelHandler(subscription::dispose);
        subscription.update(customers.findByid(id)
            .switchIfEmpty(reactor.core.publisher.Mono.error(Status.NOT_FOUND.withDescription("Customer not found").asRuntimeException()))
            .subscribe(customer -> observer.onNext(reply(customer)),
                error -> observer.onError(status(error)), observer::onCompleted));
    }

    @Override
    public void listCustomers(ListCustomersRequest request, StreamObserver<CustomerReply> observer) {
        var subscription = Disposables.swap();
        ((ServerCallStreamObserver<CustomerReply>) observer).setOnCancelHandler(subscription::dispose);
        subscription.update(customers.findAll().subscribe(customer -> observer.onNext(reply(customer)),
            error -> observer.onError(status(error)), observer::onCompleted));
    }

    private static Throwable status(Throwable error) {
        if (error instanceof io.grpc.StatusRuntimeException) return error;
        return Status.INTERNAL.withDescription("Customer query failed").asRuntimeException();
    }
    private static CustomerReply reply(Customer customer) {
        return CustomerReply.newBuilder().setId(customer.getId().value().toString())
            .setFamilyName(customer.getFamilyName() == null ? "" : customer.getFamilyName())
            .setGivenName(customer.getGivenName() == null ? "" : customer.getGivenName()).build();
    }
}
