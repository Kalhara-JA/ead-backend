package com.store.microservices.order_service.config;

import com.store.microservices.order_service.client.InventoryClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

/**
 * Configuration for setting up the RestClient and InventoryClient.
 * Handles communication with the Inventory Service.
 */
@Configuration
public class RestClientConfig {

    @Value("${inventory.url}")
    private String inventoryServiceURL;

    /**
     * Creates an InventoryClient for interacting with the Inventory Service.
     *
     * @return an instance of InventoryClient
     */
    @Bean
    public InventoryClient InventoryClient() {
        RestClient restClient = RestClient.builder()
                .baseUrl(inventoryServiceURL)
                .build();
        var restClientAdapter = RestClientAdapter.create(restClient);
        var httpServiceProxyFactory = HttpServiceProxyFactory.builderFor(restClientAdapter).build();
        return httpServiceProxyFactory.createClient(InventoryClient.class);
    }

    /**
     * Configures the HTTP request factory with timeout settings.
     *
     * @return a ClientHttpRequestFactory instance with custom settings
     */
    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        ClientHttpRequestFactorySettings clientHttpRequestFactorySettings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(3))
                .withReadTimeout(Duration.ofSeconds(3));
        return ClientHttpRequestFactories.get(clientHttpRequestFactorySettings);
    }
}
