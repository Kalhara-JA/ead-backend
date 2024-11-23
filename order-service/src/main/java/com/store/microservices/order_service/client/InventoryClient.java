package com.store.microservices.order_service.client;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;


public interface InventoryClient {
    @GetExchange("/api/inventory")
    boolean isInStock(@RequestParam String skuCode,@RequestParam Integer quantity);

    @GetExchange("api/inventory/decrementStock/{skuCode}/decrement/{quantity}")
    boolean decrementStock(@RequestParam String skuCode,@RequestParam Integer quantity);

//    Logger log = LoggerFactory.getLogger(InventoryClient.class);
//
//    @GetExchange("/api/inventory")
//    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
//    @Retry(name = "inventory")
//    boolean isInStock(@RequestParam String skuCode, @RequestParam Integer quantity);
//
//    default boolean fallbackMethod(String code, Integer quantity, Throwable throwable) {
//        log.info("Cannot get inventory for skucode {}, failure reason: {}", code, throwable.getMessage());
//        return false;
//    }

}
