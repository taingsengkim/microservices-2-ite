package kh.edu.istad.platform.customer.grpc;

import io.grpc.*;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import kh.edu.istad.common.domain.valueobject.CustomerId;
import kh.edu.istad.platform.customer.domain.entity.Customer;
import kh.edu.istad.platform.customer.domain.usecase.QueryCustomerUseCase;
import kh.edu.istad.platform.grpc.customer.v1.*;
import org.junit.jupiter.api.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerGrpcServiceTest {
    private QueryCustomerUseCase customers;
    private Server server;
    private ManagedChannel channel;
    private CustomerServiceGrpc.CustomerServiceBlockingStub client;

    @BeforeEach void start() throws Exception {
        customers = mock(QueryCustomerUseCase.class);
        String name = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(name).directExecutor()
            .addService(new CustomerGrpcService(customers)).build().start();
        channel = InProcessChannelBuilder.forName(name).directExecutor().build();
        client = CustomerServiceGrpc.newBlockingStub(channel).withDeadlineAfter(3, TimeUnit.SECONDS);
    }
    @AfterEach void stop() {
        channel.shutdownNow();
        server.shutdownNow();
    }
    @Test void rejectsInvalidUuid() {
        var error = assertThrows(StatusRuntimeException.class, () -> client.getCustomer(
            GetCustomerRequest.newBuilder().setId("invalid").build()));
        assertEquals(Status.Code.INVALID_ARGUMENT, error.getStatus().getCode());
        verifyNoInteractions(customers);
    }
    @Test void returnsNotFound() {
        UUID id = UUID.randomUUID();
        when(customers.findByid(id)).thenReturn(Mono.empty());
        var error = assertThrows(StatusRuntimeException.class, () -> client.getCustomer(
            GetCustomerRequest.newBuilder().setId(id.toString()).build()));
        assertEquals(Status.Code.NOT_FOUND, error.getStatus().getCode());
    }
    @Test void returnsCustomerAndStreamsList() {
        UUID id = UUID.randomUUID();
        var customer = Customer.builder().id(new CustomerId(id))
            .familyName("Chan").givenName("Dara").build();
        when(customers.findByid(id)).thenReturn(Mono.just(customer));
        when(customers.findAll()).thenReturn(Flux.just(customer, customer));
        var reply = client.getCustomer(GetCustomerRequest.newBuilder().setId(id.toString()).build());
        assertEquals(id.toString(), reply.getId());
        assertEquals("Chan", reply.getFamilyName());
        assertEquals("Dara", reply.getGivenName());
        var stream = client.listCustomers(ListCustomersRequest.getDefaultInstance());
        assertEquals(reply, stream.next());
        assertEquals(reply, stream.next());
        assertFalse(stream.hasNext());
    }
    @Test void hidesRepositoryFailureDetails() {
        when(customers.findAll()).thenReturn(Flux.error(new IllegalStateException("private database details")));
        var error = assertThrows(StatusRuntimeException.class, () ->
            client.listCustomers(ListCustomersRequest.getDefaultInstance()).hasNext());
        assertEquals(Status.Code.INTERNAL, error.getStatus().getCode());
        assertEquals("Customer query failed", error.getStatus().getDescription());
    }
}
