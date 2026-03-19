package com.billiards.store.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billiards.store.model.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByProductCodeAndIsDeletedFalse(String productCode);
    Optional<Product> findByIdAndIsDeletedFalse(UUID id);
    List<Product> findByIsDeletedFalse();
}
