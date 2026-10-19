package com.billiards.store.service;

import com.billiards.store.common.base.PagedResponse;
import com.billiards.store.common.enumerated.SessionStatus;
import com.billiards.store.common.exception.ResourceNotFoundException;
import com.billiards.store.common.util.CodeGeneratorUtil;
import com.billiards.store.common.util.SystemLanguage;
import com.billiards.store.dto.order_item.OrderItemCreateRequestDto;
import com.billiards.store.dto.order_item.OrderItemLineDto;
import com.billiards.store.dto.order_item.OrderItemRequestDto;
import com.billiards.store.dto.order_item.OrderItemResponseDto;
import com.billiards.store.dto.product.ProductResponseDto;
import com.billiards.store.dto.product.ProductUpdateStockDto;
import com.billiards.store.model.OrderItem;
import com.billiards.store.repository.OrderItemRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrderItemService {

	private final OrderItemRepository orderItemRepository;
	private final CodeGeneratorUtil codeGeneratorUtil;
	private final PlaySessionService playSessionService;
	private final ProductService productService;
	private final AuditService auditService;

	private static final String AUDIT_ENTITY = "OrderItem";

	public OrderItemService(
		OrderItemRepository orderItemRepository,
		CodeGeneratorUtil codeGeneratorUtil,
		PlaySessionService playSessionService,
		ProductService productService,
		AuditService auditService
	) {
		this.orderItemRepository = orderItemRepository;
		this.codeGeneratorUtil = codeGeneratorUtil;
		this.playSessionService = playSessionService;
		this.productService = productService;
		this.auditService = auditService;
	}

	/**
	 * Creates order items for several products in the specified session at once.
	 * <p>
	 * Validates the request, checks that the play session exists, then for each line retrieves the
	 * product, generates an order item code, deducts the product stock and saves the order item.
	 * The whole operation is transactional: if any line fails, no order item is created and no stock is changed.
	 * </p>
	 *
	 * @param requestDto The request data containing the session code and the list of product lines (product code, quantity).
	 * @return A list of OrderItemResponseDto representing the newly created order items, in request order.
	 */
	@Transactional
	public List<OrderItemResponseDto> create(
		OrderItemCreateRequestDto requestDto
	) {
		validateCreateRequestDto(requestDto);

		playSessionService.findBySessionCode(requestDto.getSessionCode());

		List<OrderItemResponseDto> responses = new ArrayList<>();
		for (OrderItemLineDto line : requestDto.getItems()) {
			ProductResponseDto productResponseDto =
				productService.findByProductCode(line.getProductCode());

			OrderItem orderItem = new OrderItem();
			orderItem.setOrderItemCode(
				codeGeneratorUtil.generateCode("ORI", "order_item_code_seq")
			);
			orderItem.setSessionCode(requestDto.getSessionCode());
			orderItem.setProductCode(line.getProductCode());
			orderItem.setQuantity(line.getQuantity());
			orderItem.setPrice(productResponseDto.getPrice());

			productService.updateStock(
				line.getProductCode(),
				new ProductUpdateStockDto(true, line.getQuantity())
			);

			OrderItemResponseDto responseDto = convertToResponseDto(
				orderItemRepository.save(orderItem)
			);
			responseDto.setProductName(productResponseDto.getName());
			auditService.record(
				"CREATE",
				AUDIT_ENTITY,
				orderItem.getOrderItemCode(),
				null,
				responseDto
			);
			responses.add(responseDto);
		}
		return responses;
	}

	/**
	 * Finds an order item by its unique identifier.
	 * <p>
	 * Retrieves the order item from the database using the provided ID and throws a ResourceNotFoundException
	 * if not found or already deleted. Returns the converted response DTO with all relevant details.
	 * </p>
	 *
	 * @param id The UUID of the order item to find.
	 * @return An OrderItemResponseDto containing the order item data.
	 */
	public OrderItemResponseDto findById(UUID id) {
		OrderItem orderItem = orderItemRepository
			.findByIdAndIsDeletedFalse(id)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("order_item_not_found")
				)
			);
		return convertToResponseDto(orderItem);
	}

	/**
	 * Finds an order item by its unique code.
	 * <p>
	 * Retrieves the order item from the database using the provided order item code and throws a ResourceNotFoundException
	 * if not found or already deleted. Returns the converted response DTO with all relevant details.
	 * </p>
	 *
	 * @param orderItemCode The unique code of the order item to find.
	 * @return An OrderItemResponseDto containing the order item data.
	 */
	public OrderItemResponseDto findByOrderItemCode(String orderItemCode) {
		OrderItem orderItem = orderItemRepository
			.findByOrderItemCodeAndIsDeletedFalse(orderItemCode)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("order_item_not_found")
				)
			);
		return convertToResponseDto(orderItem);
	}

	/**
	 * Finds all active order items (not deleted).
	 * <p>
	 * Retrieves all non-deleted order items from the database, converts them to response DTOs, and returns a list.
	 * </p>
	 *
	 * @return A List of OrderItemResponseDto containing all active order item data.
	 */
	public List<OrderItemResponseDto> findAll() {
		return orderItemRepository
			.findByIsDeletedFalse()
			.stream()
			.map(this::convertToResponseDto)
			.collect(Collectors.toList());
	}

	/**
	 * Retrieves a page of active order items (not deleted).
	 *
	 * @param pageable the pagination and sorting information
	 * @return a paginated response of active order items as response DTOs
	 */
	public PagedResponse<OrderItemResponseDto> findAll(Pageable pageable) {
		return PagedResponse.from(
			orderItemRepository
				.findByIsDeletedFalse(pageable)
				.map(this::convertToResponseDto)
		);
	}

	/**
	 * Calculates the total fee for a specific order item.
	 * <p>
	 * Retrieves the order item by its code and calculates the total amount as unit price multiplied by quantity.
	 * </p>
	 *
	 * @param orderItemCode The unique code of the order item to calculate fees for.
	 * @return A BigDecimal representing the total fee (unitPrice × quantity).
	 */
	public BigDecimal calculateOrderItemFnbFee(String orderItemCode) {
		OrderItem orderItem = orderItemRepository
			.findByOrderItemCodeAndIsDeletedFalse(orderItemCode)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("order_item_not_found")
				)
			);
		BigDecimal unitPrice = orderItem.getPrice();
		int quantity = orderItem.getQuantity();
		return unitPrice.multiply(BigDecimal.valueOf(quantity));
	}

	/**
	 * Calculates the total F&B fee for all order items in a play session.
	 * <p>
	 * Retrieves all active order items associated with the given play session code, then calculates
	 * and sums up the total amount (unit price × quantity) across all order items.
	 * </p>
	 *
	 * @param playSessionCode The unique code of the play session to calculate fees for.
	 * @return A BigDecimal representing the total F&B fee for all orders in the session.
	 */
	public BigDecimal calculateTotalFnbFee(String playSessionCode) {
		List<OrderItemResponseDto> allOrders = findOrderItemsBySessionCode(
			playSessionCode
		);

		BigDecimal total = new BigDecimal(0);

		for (OrderItemResponseDto order : allOrders) {
			total = total.add(
				order
					.getPrice()
					.multiply(BigDecimal.valueOf(order.getQuantity()))
			);
		}

		return total;
	}

	/**
	 * Cancels an order item by its code.
	 * <p>
	 * If the associated play session is still active, restores the product stock to previous level
	 * before marking the order item as deleted. Throws ResourceNotFoundException if:
	 * - Order item not found or already deleted
	 * - Play session has completed (not active)
	 * </p>
	 *
	 * @param orderItemCode The unique code of the order item to cancel.
	 */
	@Transactional
	public void cancelOrder(String orderItemCode) {
		OrderItem order = orderItemRepository
			.findByOrderItemCodeAndIsDeletedFalse(orderItemCode)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("order_item_not_found")
				)
			);

		if (
			playSessionService.checkCurrentSessionStatus(
				order.getSessionCode()
			) == SessionStatus.ACTIVE
		) {
			ProductUpdateStockDto productUpdateStockDto =
				new ProductUpdateStockDto(false, order.getQuantity());
			productService.updateStock(
				order.getProductCode(),
				productUpdateStockDto
			);
			OrderItemResponseDto before = convertToResponseDto(order);
			order.setIsDeleted(true);
			orderItemRepository.save(order);
			auditService.record(
				"CANCEL",
				AUDIT_ENTITY,
				orderItemCode,
				before,
				null
			);
		} else {
			throw new ResourceNotFoundException(
				SystemLanguage.getMessage("session_completed")
			);
		}
	}

	/**
	 * Updates the quantity of an order item.
	 * <p>
	 * Validates that the new quantity is positive and different from current value, checks if play session is active,
	 * adjusts product stock accordingly (restores for decrease or deducts for increase), then updates the order item quantity.
	 * Throws ResourceNotFoundException if:
	 * - Order item not found or already deleted
	 * - New quantity <= 0 or same as current quantity
	 * - Play session has completed (not active)
	 * </p>
	 *
	 * @param orderItemCode The unique code of the order item to update.
	 * @param newQuantity The new quantity for the order item.
	 */
	@Transactional
	public void updateOrderQuantity(String orderItemCode, int newQuantity) {
		OrderItem order = orderItemRepository
			.findByOrderItemCodeAndIsDeletedFalse(orderItemCode)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("order_item_not_found")
				)
			);

		if (newQuantity <= 0 || newQuantity == order.getQuantity()) {
			throw new ResourceNotFoundException(
				SystemLanguage.getMessage("invalid_quantity")
			);
		}
		if (
			playSessionService.checkCurrentSessionStatus(
				order.getSessionCode()
			) == SessionStatus.ACTIVE
		) {
			if (newQuantity < order.getQuantity()) {
				ProductUpdateStockDto productUpdateStockDto =
					new ProductUpdateStockDto(
						false,
						order.getQuantity() - newQuantity
					);
				productService.updateStock(
					order.getProductCode(),
					productUpdateStockDto
				);
			} else {
				ProductUpdateStockDto productUpdateStockDto =
					new ProductUpdateStockDto(
						true,
						newQuantity - order.getQuantity()
					);
				productService.updateStock(
					order.getProductCode(),
					productUpdateStockDto
				);
			}
			OrderItemResponseDto before = convertToResponseDto(order);
			order.setQuantity(newQuantity);
			orderItemRepository.save(order);
			auditService.record(
				"UPDATE_QUANTITY",
				AUDIT_ENTITY,
				orderItemCode,
				before,
				convertToResponseDto(order)
			);
		} else {
			throw new ResourceNotFoundException(
				SystemLanguage.getMessage("session_completed")
			);
		}
	}

	/**
	 * Updates an order item by its unique identifier.
	 * <p>
	 * Retrieves the order item, updates session code, product code, quantity from request DTO, fetches new price,
	 * saves to database, and returns updated response DTO. Throws ResourceNotFoundException if not found or deleted.
	 * </p>
	 *
	 * @param id The UUID of the order item to update.
	 * @param requestDto The request data containing session code, product code, quantity for updates.
	 * @return An OrderItemResponseDto representing the updated order item with all its details.
	 */
	@Transactional
	public OrderItemResponseDto update(
		UUID id,
		OrderItemRequestDto requestDto
	) {
		OrderItem orderItem = orderItemRepository
			.findByIdAndIsDeletedFalse(id)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("order_item_not_found")
				)
			);

		OrderItemResponseDto before = convertToResponseDto(orderItem);
		orderItem.setSessionCode(requestDto.getSessionCode());
		orderItem.setProductCode(requestDto.getProductCode());
		orderItem.setQuantity(requestDto.getQuantity());

		orderItem.setPrice(
			productService.getProductPrice(requestDto.getProductCode())
		);

		OrderItem updatedOrderItem = orderItemRepository.save(orderItem);
		OrderItemResponseDto result = convertToResponseDto(updatedOrderItem);
		auditService.record(
			"UPDATE",
			AUDIT_ENTITY,
			orderItem.getOrderItemCode(),
			before,
			result
		);
		return result;
	}

	/**
	 * Finds all active order items for a specific play session.
	 * <p>
	 * Retrieves all non-deleted order items associated with the given play session code, converts them to response DTOs, and returns as list.
	 * </p>
	 *
	 * @param sessionCode The unique code of the play session to find orders for.
	 * @return A List of OrderItemResponseDto containing all active order items in the session.
	 */
	public List<OrderItemResponseDto> findOrderItemsBySessionCode(
		String sessionCode
	) {
		return orderItemRepository
			.findAllBySessionCodeAndIsDeletedFalse(sessionCode)
			.stream()
			.map(this::convertToResponseDto)
			.collect(Collectors.toList());
	}

	/**
	 * Deletes an order item by its unique identifier.
	 * <p>
	 * Retrieves the order item, adjusts product stock (restores it), marks the order item as deleted,
	 * and saves to the database. Throws ResourceNotFoundException if not found or already deleted.
	 * </p>
	 *
	 * @param id The UUID of the order item to delete.
	 */
	@Transactional
	public void delete(UUID id) {
		OrderItem orderItem = orderItemRepository
			.findByIdAndIsDeletedFalse(id)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("order_item_not_found")
				)
			);
		ProductUpdateStockDto productUpdateStockDto = new ProductUpdateStockDto(
			false,
			orderItem.getQuantity()
		);
		productService.updateStock(
			orderItem.getProductCode(),
			productUpdateStockDto
		);
		OrderItemResponseDto before = convertToResponseDto(orderItem);
		orderItem.setIsDeleted(true);
		orderItemRepository.save(orderItem);
		auditService.record(
			"DELETE",
			AUDIT_ENTITY,
			orderItem.getOrderItemCode(),
			before,
			null
		);
	}

	/**
	 * Validates the create request: session code must be present, the item list must be non-empty,
	 * and every line needs a product code and a positive quantity.
	 * Throws ResourceNotFoundException for invalid inputs.
	 *
	 * @param requestDto The create request to validate.
	 */
	private void validateCreateRequestDto(
		OrderItemCreateRequestDto requestDto
	) {
		if (
			requestDto.getSessionCode() == null ||
			requestDto.getSessionCode().trim().isEmpty()
		) {
			throw new ResourceNotFoundException(
				SystemLanguage.getMessage("session_code_empty")
			);
		}
		if (requestDto.getItems() == null || requestDto.getItems().isEmpty()) {
			throw new ResourceNotFoundException(
				SystemLanguage.getMessage("product_code_empty")
			);
		}
		for (OrderItemLineDto line : requestDto.getItems()) {
			if (
				line == null ||
				line.getProductCode() == null ||
				line.getProductCode().trim().isEmpty()
			) {
				throw new ResourceNotFoundException(
					SystemLanguage.getMessage("product_code_empty")
				);
			}
			if (line.getQuantity() <= 0) {
				throw new ResourceNotFoundException(
					SystemLanguage.getMessage("invalid_quantity")
				);
			}
		}
	}

	/**
	 * Validates the request DTO parameters.
	 * <p>
	 * Checks that session code and product code are not null or empty, and quantity is positive.
	 * Throws ResourceNotFoundException for invalid inputs.
	 * </p>
	 *
	 * @param requestDto The request data to validate.
	 */
	private void validateRequestDto(OrderItemRequestDto requestDto) {
		if (
			requestDto.getSessionCode() == null ||
			requestDto.getSessionCode().trim().isEmpty()
		) {
			throw new ResourceNotFoundException(
				SystemLanguage.getMessage("session_code_empty")
			);
		}
		if (
			requestDto.getProductCode() == null ||
			requestDto.getProductCode().trim().isEmpty()
		) {
			throw new ResourceNotFoundException(
				SystemLanguage.getMessage("product_code_empty")
			);
		}
		if (requestDto.getQuantity() <= 0) {
			throw new ResourceNotFoundException(
				SystemLanguage.getMessage("invalid_quantity")
			);
		}
	}

	/**
	 * Converts an OrderItem entity to its corresponding response DTO.
	 * <p>
	 * Maps all fields from the OrderItem entity to the OrderItemResponseDto, including timestamps and user information.
	 * </p>
	 *
	 * @param orderItem The order item entity to convert.
	 * @return The converted OrderItemResponseDto.
	 */
	private OrderItemResponseDto convertToResponseDto(OrderItem orderItem) {
		OrderItemResponseDto dto = new OrderItemResponseDto();
		dto.setOrderItemCode(orderItem.getOrderItemCode());
		dto.setSessionCode(orderItem.getSessionCode());
		dto.setProductCode(orderItem.getProductCode());
		dto.setQuantity(orderItem.getQuantity());
		dto.setPrice(orderItem.getPrice());
		dto.setCreatedAt(orderItem.getCreatedAt());
		dto.setUpdatedAt(orderItem.getUpdatedAt());
		dto.setCreatedBy(orderItem.getCreatedBy());
		dto.setUpdatedBy(orderItem.getUpdatedBy());
		return dto;
	}

	/**
	 * Convert a simple array of values into an OrderItemResponseDto.
	 *
	 * @param object An array containing: [productCode, quantity, price, productName]
	 * @return The converted OrderItemResponseDto
	 */
	private OrderItemResponseDto convertToResponseDto(Object[] object) {
		OrderItemResponseDto dto = new OrderItemResponseDto();
		dto.setProductCode((String) object[0]);
		dto.setQuantity((int) object[1]);
		dto.setPrice((BigDecimal) object[2]);
		dto.setProductName((String) object[3]);
		return dto;
	}
}
