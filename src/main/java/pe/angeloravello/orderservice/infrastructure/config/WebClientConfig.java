package pe.angeloravello.orderservice.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${product-service.base-url}")
    private String productServiceBaseUrl;

    @Bean
    public WebClient productWebClient() {
        return WebClient.builder()
                .baseUrl(productServiceBaseUrl)
                .build();
    }
}
