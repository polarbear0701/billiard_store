package com.billiards.store.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import com.billiards.store.common.base.PagedResponse;
import com.billiards.store.common.base.SuccessResponse;
import com.billiards.store.common.util.SystemLanguage;
import com.billiards.store.dto.product.ProductRequestDto;
import com.billiards.store.dto.product.ProductResponseDto;
import com.billiards.store.dto.product.ProductUpdateStockDto;
import com.billiards.store.service.ProductService;
import java.time.LocalDateTime;
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
@RequestMapping("/product")
@Tag(
	name = "Product",
	description = "Stock items sold to customers (drinks, snacks). Reading is open to STAFF; changing the catalogue or stock is ADMIN only."
)
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@Operation(
		operationId = "product_create",
		summary = "Create a product",
		description = "Creates a product with a generated code such as `PRD000001`. Access: ADMIN only."
	)
	@PostMapping("/create")
	public ResponseEntity<SuccessResponse<ProductResponseDto>> create(
		@RequestBody ProductRequestDto request
	) {
		ProductResponseDto responseDto = productService.create(request);
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
		operationId = "product_findAll",
		summary = "List products (paged)",
		description = "Returns non-deleted products, newest first by default. Access: ADMIN, STAFF."
	)
	@GetMapping("/all")
	public ResponseEntity<SuccessResponse<PagedResponse<ProductResponseDto>>> findAll(
		@Parameter(description = "Zero-based page index", example = "0") @RequestParam(defaultValue = "0") int page,
		@Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20") int size,
		@Parameter(description = "Field of the returned items to sort by, e.g. `createdAt` or `updatedAt`", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy,
		@Parameter(description = "Sort direction: `asc` or `desc`", example = "desc") @RequestParam(defaultValue = "desc") String direction
	) {
		Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
		Pageable pageable = PageRequest.of(page, size, sort);
		PagedResponse<ProductResponseDto> productsDto = productService.findAll(
			pageable
		);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("success"),
				productsDto,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "product_findById",
		summary = "Get a product by internal id",
		description = "Looks a product up by its UUID. Most other endpoints use the product code instead. Access: ADMIN, STAFF."
	)
	@GetMapping("/{id}")
	public ResponseEntity<SuccessResponse<ProductResponseDto>> findById(@Parameter(description = "Internal UUID of the record", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") @PathVariable UUID id) {
		ProductResponseDto responseDto = productService.findById(id);
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
		operationId = "product_findByProductCode",
		summary = "Get a product by code",
		description = "Looks a product up by its code, e.g. `PRD000001`. Access: ADMIN, STAFF."
	)
	@GetMapping("/product-code/{productCode}")
	public ResponseEntity<SuccessResponse<ProductResponseDto>> findByProductCode(
		@Parameter(description = "Product code", example = "PRD000001") @PathVariable String productCode
	) {
		ProductResponseDto productDto = productService.findByProductCode(
			productCode
		);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("success"),
				productDto,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "product_updateProductName",
		summary = "Rename a product",
		description = "Changes only the name. `data` is a confirmation message. Access: ADMIN only."
	)
	@PutMapping("/update/name/{productCode}")
	public ResponseEntity<SuccessResponse<String>> updateProductName(
		@Parameter(description = "Product code", example = "PRD000001") @PathVariable String productCode,
		@Parameter(description = "New product name", example = "Coca-Cola can") @RequestParam String newProductName
	) {
		productService.updateStockName(productCode, newProductName);

		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				SystemLanguage.getMessage("update_success"),
				String.format(
					"Product name has been changed to %s",
					newProductName
				),
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "product_updateProductQuantity",
		summary = "Increase or decrease stock",
		description = "With `decrement=true` the amount is subtracted (fails if there is not enough stock); with `decrement=false` it is added. Use this for restocking. Access: ADMIN only."
	)
	@PutMapping("/update/quantity/{productCode}")
	public ResponseEntity<SuccessResponse<Void>> updateProductQuantity(
		@Parameter(description = "Product code", example = "PRD000001") @PathVariable String productCode,
		@RequestBody ProductUpdateStockDto productUpdateStockDto
	) {
		productService.updateStock(productCode, productUpdateStockDto);

		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.ACCEPTED.value(),
				SystemLanguage.getMessage("update_success"),
				null,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "product_clearStockQuantity",
		summary = "Set stock to zero",
		description = "Sets the quantity to 0. Access: ADMIN only."
	)
	@PutMapping("/update/clear/{productCode}")
	public ResponseEntity<SuccessResponse<Void>> clearStockQuantity(
		@Parameter(description = "Product code", example = "PRD000001") @PathVariable String productCode
	) {
		productService.clearStock(productCode);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.ACCEPTED.value(),
				SystemLanguage.getMessage("delete_success"),
				null,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "product_updateProductPrice",
		summary = "Change a product price",
		description = "Sets the unit price in VND. The new price must be greater than 1000, otherwise the request is rejected. Access: ADMIN only."
	)
	@PutMapping("/update/price/{productCode}")
	public ResponseEntity<SuccessResponse<Void>> updateProductPrice(
		@Parameter(description = "Product code", example = "PRD000001") @PathVariable String productCode,
		@Parameter(description = "New unit price in VND, must be greater than 1000", example = "15000") @RequestParam("newPrice") int newStockPrice
	) {
		productService.updatePrice(productCode, newStockPrice);

		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.ACCEPTED.value(),
				SystemLanguage.getMessage("update_success"),
				null,
				LocalDateTime.now()
			)
		);
	}

	@Operation(
		operationId = "product_delete",
		summary = "Delete a product",
		description = "Soft delete: the product disappears from all lists but past orders keep working. Access: ADMIN only."
	)
	@DeleteMapping("/{productCode}")
	public ResponseEntity<SuccessResponse<Void>> delete(
		@Parameter(description = "Product code", example = "PRD000001") @PathVariable String productCode
	) {
		productService.delete(productCode);
		return ResponseEntity.ok(
			new SuccessResponse<>(
				HttpStatus.OK.value(),
				"Product deleted successfully",
				null,
				LocalDateTime.now()
			)
		);
	}
}
