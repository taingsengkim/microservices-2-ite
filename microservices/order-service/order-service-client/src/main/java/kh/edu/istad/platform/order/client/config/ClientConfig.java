package kh.edu.istad.platform.order.client.config;

import kh.edu.istad.platform.order.client.httpclient.BusinessHttpClient;
import kh.edu.istad.platform.order.client.httpclient.CustomerHttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.reactive.function.client.support.WebClientHttpServiceGroupConfigurer;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.service.registry.HttpServiceGroup;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(
        group = "customer-service",
        types = {CustomerHttpClient.class},
        clientType = HttpServiceGroup.ClientType.WEB_CLIENT
)
@ImportHttpServices(
        group = "business-service",
        types = {BusinessHttpClient.class},
        clientType = HttpServiceGroup.ClientType.WEB_CLIENT
)
//@RequiredArgsConstructor
public class ClientConfig {

    public WebClientHttpServiceGroupConfigurer webClientHttpServiceGroupConfigurer(){
        return groups -> {
            groups.filterByName("customer-service")
                    .forEachClient(
                            (group,clientBuilder)->{
                                clientBuilder
                                        .defaultHeader("User-Agent","Web-cliebnt")
                                        .defaultStatusHandler(
                                                HttpStatusCode::isError,
                                                clientResponse -> {
                                                    System.out.println("Exception occoured");
                                                    return clientResponse.createException();
                                                }
                                        );
                            }
                    );
        };
    }



//    private final WebClient.Builder webClient;
//
//    @Bean
//    public CustomerHttpClient customerHttpClient() {
//        return createClient("http://customer-service", CustomerHttpClient.class);
//    }
//
//    @Bean
//    public BusinessHttpClient businessHttpClient() {
//        return createClient("http://business-service", BusinessHttpClient.class);
//    }
//
//    private <T> T createClient(String url, Class<T> type) {
//        WebClient wc = webClient.baseUrl(url).build();
//
//        HttpServiceProxyFactory factory  = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(wc)).build();
//        return factory.createClient(type);
//    }
//
//    private <T> T createClient(WebClient wc, Class<T> type) {
//        HttpServiceProxyFactory factory  = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(wc)).build();
//        return factory.createClient(type);
//    }



}


