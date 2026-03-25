package com.billiards.store.controller;

import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import com.billiards.store.common.base.SuccessResponse;
import com.billiards.store.dto.product.ProductRequestDto;
import com.billiards.store.dto.product.ProductResponseDto;
import com.billiards.store.dto.product.ProductUpdateStockDto;
import com.billiards.store.service.ProductService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
    public ResponseEntity<SuccessResponse<?>> create(@RequestBody ProductRequestDto request) {
        ProductResponseDto responseDto = productService.create(request);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.ACCEPTED.value(),
                "success",
                responseDto,
                LocalDateTime.now()
        ));
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<?>> findAll() {
        List<ProductResponseDto> productsDto = productService.findAll();
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "success",
                productsDto,
                LocalDateTime.now()
        ));
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
    public ResponseEntity<SuccessResponse<?>> findByProductCode(@PathVariable String productCode) {
        ProductResponseDto productDto = productService.findByProductCode(productCode);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "success",
                productDto,
                LocalDateTime.now()
        ));
    }

    @PutMapping("/update-stock/quantity/{productCode}")
    public ResponseEntity<SuccessResponse<?>> updateProductQuantity(@PathVariable String productCode, @RequestBody ProductUpdateStockDto productUpdateStockDto) {
        productService.updateStock(productCode, productUpdateStockDto.getAmountToUpdate(), productUpdateStockDto.isDecrement());
        
        return ResponseEntity.ok(new SuccessResponse<>(
            HttpStatus.ACCEPTED.value(), 
            "success", 
            null, 
            LocalDateTime.now()));
    }

    @PutMapping("/update-stock/price/{productCode}")
    public ResponseEntity<SuccessResponse<?>> updateProductPrice(@PathVariable String productCode, @RequestParam("newPrice") int newStockPrice) {
        productService.updatePrice(productCode, newStockPrice);
        
        return ResponseEntity.ok(new SuccessResponse<>(
            HttpStatus.ACCEPTED.value(), 
            "success", 
            null, 
            LocalDateTime.now()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<?>> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Product deleted successfully",
                null,
                LocalDateTime.now()
        ));
    }
}

