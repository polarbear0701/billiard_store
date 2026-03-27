package com.billiards.store.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.billiards.store.common.exception.ResourceNotFoundException;
import com.billiards.store.common.util.CodeGeneratorUtil;
import com.billiards.store.dto.product.ProductRequestDto;
import com.billiards.store.dto.product.ProductResponseDto;
import com.billiards.store.dto.product.ProductUpdateStockDto;
import com.billiards.store.model.Product;
import com.billiards.store.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CodeGeneratorUtil codeGeneratorUtil;

    public ProductService(
        ProductRepository productRepository,
        CodeGeneratorUtil codeGeneratorUtil
    ) {
        this.productRepository = productRepository;
        this.codeGeneratorUtil = codeGeneratorUtil;
    }

    public ProductResponseDto create(ProductRequestDto requestDto) {
        Product product = new Product();
        product.setProductCode(
            codeGeneratorUtil.generateCode("PRD", "product_code_seq")
        );
        product.setName(requestDto.getName());
        product.setQuantity(requestDto.getQuantity());
        product.setPrice(requestDto.getPrice());
        
        Product savedProduct = productRepository.save(product);
        return convertToResponseDto(savedProduct);
    }

    public BigDecimal getProductPrice(String productCode) {
        Product product = productRepository.findByProductCodeAndIsDeletedFalse(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("No product found"));
        return product.getPrice();
    }

    public ProductResponseDto findById(UUID id) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return convertToResponseDto(product);
    }

    public ProductResponseDto findByProductCode(String productCode) {
        Product product = productRepository.findByProductCodeAndIsDeletedFalse(productCode)
            .orElseThrow(() -> new ResourceNotFoundException("No product associated with this product code. Please try again"));
        return convertToResponseDto(product);
    }

    public List<ProductResponseDto> findAll() {
        return productRepository.findByIsDeletedFalse().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public ProductResponseDto update(String productCode, ProductRequestDto requestDto) {
        Product product = productRepository.findByProductCodeAndIsDeletedFalse(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setName(requestDto.getName());
        product.setQuantity(requestDto.getQuantity());
        product.setPrice(requestDto.getPrice());
        
        Product updatedProduct = productRepository.save(product);
        return convertToResponseDto(updatedProduct);
    }

    public void updateStock(String productCode, ProductUpdateStockDto productUpdateStockDto) {
        Product product = productRepository.findByProductCodeAndIsDeletedFalse(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (productUpdateStockDto.isDecrement()) {
            if (product.getQuantity() < productUpdateStockDto.getAmountToUpdate()) {
                throw new ResourceNotFoundException("Not enough stock available");
            }

            product.setQuantity(product.getQuantity() - productUpdateStockDto.getAmountToUpdate());
        } else {
            product.setQuantity(product.getQuantity() + productUpdateStockDto.getAmountToUpdate());
        }

        productRepository.save(product);
    }

    public void updatePrice(String productCode, int newStockPrice) {
        Product product = productRepository.findByProductCodeAndIsDeletedFalse(productCode)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (newStockPrice <= 1000) {
            throw new ResourceNotFoundException("Stock price is invalid");
        }

        product.setPrice(BigDecimal.valueOf(newStockPrice));
        productRepository.save(product);
    }

    public void delete(UUID id) {
        Product product = productRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setIsDeleted(true);
        productRepository.save(product);
    }

    private ProductResponseDto convertToResponseDto(Product product) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.setProductCode(product.getProductCode());
        dto.setName(product.getName());
        dto.setQuantity(product.getQuantity());
        dto.setPrice(product.getPrice());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setCreatedBy(product.getCreatedBy());
        dto.setUpdatedBy(product.getUpdatedBy());
        return dto;
    }
}
