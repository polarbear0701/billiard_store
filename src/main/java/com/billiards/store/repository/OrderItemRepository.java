package com.billiards.store.repository;

import com.billiards.store.model.OrderItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for OrderItem entity operations.
 * Provides methods for finding order items and retrieving items by session code.
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
	/**
	 * Finds an order item by its code if it exists and is not deleted.
	 * @param orderItemCode The unique order item code
	 * @return Optional containing the order item if found and not deleted, or empty otherwise
	 */
	Optional<OrderItem> findByOrderItemCodeAndIsDeletedFalse(
		String orderItemCode
	);

	/**
	 * Finds an order item by its ID if it exists and is not deleted.
	 * @param id The unique order item identifier
	 * @return Optional containing the order item if found and not deleted, or empty otherwise
	 */
	Optional<OrderItem> findByIdAndIsDeletedFalse(UUID id);

	/**
	 * Returns all order items that are not marked as deleted.
	 * @return List of all non-deleted order items
	 */
	List<OrderItem> findByIsDeletedFalse();

	/**
	 * Returns a page of order items that are not marked as deleted.
	 * @param pageable the pagination and sorting information
	 * @return a page of non-deleted order items
	 */
	Page<OrderItem> findByIsDeletedFalse(Pageable pageable);

	/**
	 * Retrieves all order items for a specific session along with product information.
	 * Returns label-value tuples containing product code, quantity, price, and product name.
	 * @param sessionCode The session code to filter by
	 * @return List of Object[] arrays containing [product_code, quantity, price, name] tuples
	 */
	@Query(
		value = "SELECT o.product_code, o.quantity, o.price, p.\"name\" " +
			"FROM order_item AS o " +
			"INNER JOIN product AS p ON o.product_code = p.product_code " +
			"WHERE o.session_code = :sessionCode",
		nativeQuery = true
	)
	List<Object[]> findAllBySessionCodeAndIsDeletedFalse(
		@Param("sessionCode") String sessionCode
	);
}
