package com.billiards.store.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import com.billiards.store.common.base.PagedResponse;
import com.billiards.store.common.base.SuccessResponse;
import com.billiards.store.common.util.SystemLanguage;
import com.billiards.store.dto.order_item.OrderItemCreateRequestDto;
import com.billiards.store.dto.order_item.OrderItemResponseDto;
import com.billiards.store.service.OrderItemService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order-item")
@Tag(
	name = "Order Item",
	description = "Food and drink ordered during a play session. Open to STAFF, except deleting an order line by id (ADMIN only)."
)
public class OrderItemController {

	private final OrderItemService orderItemService;

	public OrderItemController(OrderItemService orderItemService) {
		this.orderItemService = orderItemService;
	}

	@Operation(
		operationId = "orderItem_create",
		summary = "Add products to a session's order",
		description = "Adds several products at once (`items`). The session must exist; each line's quantity must be greater than 0 and is deducted from stock. The price is copied from the product at the time of ordering. `data` is the list of created order lines. Access: ADMIN, STAFF."
	)
	@PostMapping("/create")
	public ResponseEntity<SuccessResponse<List<OrderItemResponseDto>>> create(
		@RequestBody OrderItemCreateRequestDto request
	) {
		List<OrderItemResponseDto> responseDto = orderItemService.create(
			request
		);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.ACCEPTED.value(),
				SystemLanguage.getMessage("create_success"),
				responseDto,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "orderItem_cancelOrder",
		summary = "Cancel an order line",
		description = "Only allowed while the session is still ACTIVE. The quantity is returned to stock. Access: ADMIN, STAFF."
	)
	@PostMapping("/cancel/{orderItemCode}")
	public ResponseEntity<SuccessResponse<Void>> cancelOrder(
		@Parameter(description = "Order line code", example = "ORI000001") @PathVariable String orderItemCode
	) {
		orderItemService.cancelOrder(orderItemCode);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("delete_success"),
				null,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "orderItem_findAll",
		summary = "List order lines (paged)",
		description = "Returns non-deleted order lines across all sessions. Access: ADMIN, STAFF."
	)
	@GetMapping("/all")
	public ResponseEntity<SuccessResponse<PagedResponse<OrderItemResponseDto>>> findAll(
		@Parameter(description = "Zero-based page index", example = "0") @RequestParam(defaultValue = "0") int page,
		@Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
		@Parameter(description = "Field of the returned items to sort by, e.g. `createdAt` or `updatedAt`", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
		@Parameter(description = "Sort direction: `asc` or `desc`", example = "desc") @RequestParam(defaultValue = "desc") String direction
	) {
		Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
		Pageable pageable = PageRequest.of(page, size, sort);
		PagedResponse<OrderItemResponseDto> itemsDto = orderItemService.findAll(
			pageable
		);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("success"),
				itemsDto,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "orderItem_findById",
		summary = "Get an order line by internal id",
		description = "Looks an order line up by its UUID. Access: ADMIN, STAFF."
	)
	@GetMapping("/{id}")
	public ResponseEntity<SuccessResponse<OrderItemResponseDto>> findById(@Parameter(description = "Internal UUID of the record", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") @PathVariable UUID id) {
		OrderItemResponseDto responseDto = orderItemService.findById(id);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("success"),
				responseDto,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "orderItem_findByOrderItemCode",
		summary = "Get an order line by code",
		description = "Looks an order line up by its code, e.g. `ORI000001`. Access: ADMIN, STAFF."
	)
	@GetMapping("/order-item-code/{orderItemCode}")
	public ResponseEntity<SuccessResponse<OrderItemResponseDto>> findByOrderItemCode(
		@Parameter(description = "Order line code", example = "ORI000001") @PathVariable String orderItemCode
	) {
		OrderItemResponseDto itemDto = orderItemService.findByOrderItemCode(
			orderItemCode
		);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("success"),
				itemDto,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "orderItem_getAllOrderFromSessionCode",
		summary = "List a session's order lines",
		description = "Returns all non-cancelled order lines of one session. Use it to show the running bill. Access: ADMIN, STAFF."
	)
	@GetMapping("/all/{sessionCode}")
	public ResponseEntity<SuccessResponse<List<OrderItemResponseDto>>> getAllOrderFromSessionCode(
		@Parameter(description = "Play session code", example = "SES000001") @PathVariable String sessionCode
	) {
		List<OrderItemResponseDto> itemsDto =
			orderItemService.findOrderItemsBySessionCode(sessionCode);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("success"),
				itemsDto,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "orderItem_updateOrderItemQuantity",
		summary = "Change an order line's quantity",
		description = "The new quantity must be greater than 0 and different from the current one, and the session must be ACTIVE. Stock is adjusted by the difference. Access: ADMIN, STAFF."
	)
	@PutMapping("/update/quantity")
	public ResponseEntity<SuccessResponse<Void>> updateOrderItemQuantity(
		@Parameter(description = "Order line code", example = "ORI000001") @RequestParam String orderItemCode,
		@Parameter(description = "New quantity, greater than 0 and different from the current one", example = "3") @RequestParam int newQuantity
	) {
		orderItemService.updateOrderQuantity(orderItemCode, newQuantity);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("update_success"),
				null,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "orderItem_delete",
		summary = "Delete an order line",
		description = "Soft delete by internal id; the quantity is returned to stock. Unlike cancel, this works on finished sessions too. Access: ADMIN only."
	)
	@DeleteMapping("/{id}")
	public ResponseEntity<SuccessResponse<Void>> delete(@Parameter(description = "Internal UUID of the record", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") @PathVariable UUID id) {
		orderItemService.delete(id);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("delete_success"),
				null,
				LocalDateTime.now()
			)
		);
	}
}
