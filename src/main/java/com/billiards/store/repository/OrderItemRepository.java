package com.billiards.store.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.billiards.store.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    Optional<OrderItem> findByOrderItemCodeAndIsDeletedFalse(String orderItemCode);
    Optional<OrderItem> findByIdAndIsDeletedFalse(UUID id);
    List<OrderItem> findByIsDeletedFalse();

    @Query(
        value = "SELECT o.product_code, o.quantity, o.price, p.\"name\" " +
                "FROM order_item AS o " +
                "INNER JOIN product AS p ON o.product_code = p.product_code " +
                "WHERE o.session_code = :sessionCode", 
        nativeQuery = true
    )
    List<Object[]> findAllBySessionCodeAndIsDeletedFalse(@Param("sessionCode")String sessionCode);
}
