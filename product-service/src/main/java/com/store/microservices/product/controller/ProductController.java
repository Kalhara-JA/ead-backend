package com.store.microservices.product.controller;

import com.store.microservices.product.dto.ProductRequest;
import com.store.microservices.product.dto.ProductResponse;
import com.store.microservices.product.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@RequestBody ProductRequest productRequest) {
        return productService.createProduct(productRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProducts() {
        log.info("Getting all products");
        List<ProductResponse> responses = productService.getAllProducts();
        log.info("Products: {}", responses);
        return responses;
    }
}
