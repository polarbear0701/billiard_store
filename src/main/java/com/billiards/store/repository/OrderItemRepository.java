package com.billiards.store.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billiards.store.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    Optional<OrderItem> findByOrderItemCodeAndIsDeletedFalse(String orderItemCode);
    Optional<OrderItem> findByIdAndIsDeletedFalse(UUID id);
    List<OrderItem> findByIsDeletedFalse();
    List<OrderItem> findAllBySessionCodeAndIsDeletedFlase(String sessionCode);
}
