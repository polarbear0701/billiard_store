package com.billiards.store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import com.billiards.store.common.base.SuccessResponse;
import com.billiards.store.dto.product.ProductRequestDto;
import com.billiards.store.dto.product.ProductResponseDto;
import com.billiards.store.service.ProductService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    @PostMapping("/create")
    public ProductResponseDto create(@RequestBody ProductRequestDto request) {
        return productService.create(request);
    }

    @GetMapping
    public List<ProductResponseDto> findAll() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<?>> findById(@PathVariable UUID id) {
        ProductResponseDto responseDto = productService.findById(id);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "success",
                responseDto,
                LocalDateTime.now()

        ));
    }

    @GetMapping("/product-code/{productCode}")
    public ProductResponseDto findByProductCode(@PathVariable String productCode) {
        return productService.findByProductCode(productCode);
    }

    @PutMapping("/{id}")
    public ProductResponseDto update(@PathVariable UUID id, @RequestBody ProductRequestDto request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        productService.delete(id);
    }
}

