package kh.edu.istad.platform.customer.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;

@Configuration
public class GrpcServerConfig {
    @Bean(destroyMethod = "shutdownNow")
    Server customerGrpcServer(CustomerGrpcService service,
            @Value("${customer.grpc.port:9093}") int port) throws IOException {
        return ServerBuilder.forPort(port).addService(service).build().start();
    }
}
