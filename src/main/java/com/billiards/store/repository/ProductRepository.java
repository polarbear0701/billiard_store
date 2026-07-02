package com.billiards.store.repository;

import com.billiards.store.model.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {
	Optional<Product> findByProductCodeAndIsDeletedFalse(String productCode);
	Optional<Product> findByIdAndIsDeletedFalse(UUID id);
	List<Product> findByIsDeletedFalse();
	Page<Product> findByIsDeletedFalse(Pageable pageable);
}
