package com.billiards.store.service;

import com.billiards.store.common.base.PagedResponse;
import com.billiards.store.common.exception.ResourceNotFoundException;
import com.billiards.store.common.util.CodeGeneratorUtil;
import com.billiards.store.common.util.SystemLanguage;
import com.billiards.store.dto.product.ProductRequestDto;
import com.billiards.store.dto.product.ProductResponseDto;
import com.billiards.store.dto.product.ProductUpdateStockDto;
import com.billiards.store.model.Product;
import com.billiards.store.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
/**
 * Service for managing products in the store management system.
 * Provides operations for creating, reading, updating, and deleting products,
 * as well as managing product stock and prices.
 */
public class ProductService {

	private static final String AUDIT_ENTITY = "Product";

	private final ProductRepository productRepository;
	private final CodeGeneratorUtil codeGeneratorUtil;
	private final AuditService auditService;

	public ProductService(
		ProductRepository productRepository,
		CodeGeneratorUtil codeGeneratorUtil,
		AuditService auditService
	) {
		this.productRepository = productRepository;
		this.codeGeneratorUtil = codeGeneratorUtil;
		this.auditService = auditService;
	}

	/**
	 * Creates a new product with the provided details.
	 * Automatically generates a unique product code using the code generator utility.
	 *
	 * @param requestDto The product request DTO containing name, quantity, and price
	 * @return The created product as a response DTO
	 */
	@Transactional
	public ProductResponseDto create(ProductRequestDto requestDto) {
		Product product = new Product();
		product.setProductCode(
			codeGeneratorUtil.generateCode("PRD", "product_code_seq")
		);
		product.setName(requestDto.getName());
		product.setQuantity(requestDto.getQuantity());
		product.setPrice(requestDto.getPrice());

		Product savedProduct = productRepository.save(product);
		ProductResponseDto result = convertToResponseDto(savedProduct);
		auditService.record(
			"CREATE",
			AUDIT_ENTITY,
			savedProduct.getProductCode(),
			null,
			result
		);
		return result;
	}

	/**
	 * Retrieves the price of a product by its product code.
	 *
	 * @param productCode The unique identifier of the product
	 * @return The product's price as a BigDecimal
	 * @throws ResourceNotFoundException if the product is not found
	 */
	public BigDecimal getProductPrice(String productCode) {
		Product product = productRepository
			.findByProductCodeAndIsDeletedFalse(productCode)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("product_not_found")
				)
			);
		return product.getPrice();
	}

	/**
	 * Finds a product by its unique identifier.
	 *
	 * @param id The UUID of the product
	 * @return The product as a response DTO
	 * @throws ResourceNotFoundException if the product is not found
	 */
	public ProductResponseDto findById(UUID id) {
		Product product = productRepository
			.findByIdAndIsDeletedFalse(id)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("product_not_found")
				)
			);
		return convertToResponseDto(product);
	}

	/**
	 * Finds a product by its product code.
	 *
	 * @param productCode The product code identifier
	 * @return The product as a response DTO
	 * @throws ResourceNotFoundException if the product is not found
	 */
	public ProductResponseDto findByProductCode(String productCode) {
		Product product = productRepository
			.findByProductCodeAndIsDeletedFalse(productCode)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("product_not_found")
				)
			);
		return convertToResponseDto(product);
	}

	/**
	 * Retrieves all active products that have not been deleted.
	 *
	 * @return A list of all active products as response DTOs
	 */
	public List<ProductResponseDto> findAll() {
		return productRepository
			.findByIsDeletedFalse()
			.stream()
			.map(this::convertToResponseDto)
			.collect(Collectors.toList());
	}

	/**
	 * Retrieves a page of active products that have not been deleted.
	 *
	 * @param pageable the pagination and sorting information
	 * @return a paginated response of active products as response DTOs
	 */
	public PagedResponse<ProductResponseDto> findAll(Pageable pageable) {
		return PagedResponse.from(
			productRepository
				.findByIsDeletedFalse(pageable)
				.map(this::convertToResponseDto)
		);
	}

	/**
	 * Updates an existing product with the provided details.
	 * The product is located by its product code, and all fields (name, quantity, price)
	 * are updated from the request DTO.
	 *
	 * @param productCode The product code of the product to update
	 * @param requestDto The product request DTO containing the new name, quantity, and price
	 * @return The updated product as a response DTO
	 * @throws ResourceNotFoundException if the product is not found
	 */
	@Transactional
	public ProductResponseDto update(
		String productCode,
		ProductRequestDto requestDto
	) {
		Product product = productRepository
			.findByProductCodeAndIsDeletedFalse(productCode)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("product_not_found")
				)
			);

		ProductResponseDto before = convertToResponseDto(product);

		product.setName(requestDto.getName());
		product.setQuantity(requestDto.getQuantity());
		product.setPrice(requestDto.getPrice());

		Product updatedProduct = productRepository.save(product);
		ProductResponseDto result = convertToResponseDto(updatedProduct);
		auditService.record(
			"UPDATE",
			AUDIT_ENTITY,
			productCode,
			before,
			result
		);
		return result;
	}

	/**
	 * Updates the stock quantity of a product.
	 * If decrement is true, subtracts the amount from current stock (throws error if insufficient).
	 * If decrement is false, adds the amount to current stock.
	 *
	 * @param productCode The product code of the product
	 * @param productUpdateStockDto Contains the amount to update and whether it's a decrement or increment
	 * @throws ResourceNotFoundException if the product is not found or insufficient stock for decrement
	 */
	@Transactional
	public void updateStock(
		String productCode,
		ProductUpdateStockDto productUpdateStockDto
	) {
		Product product = productRepository
			.findByProductCodeAndIsDeletedFalse(productCode)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("product_not_found")
				)
			);
		ProductResponseDto before = convertToResponseDto(product);
		if (productUpdateStockDto.isDecrement()) {
			if (
				product.getQuantity() <
				productUpdateStockDto.getAmountToUpdate()
			) {
				throw new ResourceNotFoundException(
					SystemLanguage.getMessage("insufficient_stock")
				);
			}

			product.setQuantity(
				product.getQuantity() -
					productUpdateStockDto.getAmountToUpdate()
			);
		} else {
			product.setQuantity(
				product.getQuantity() +
					productUpdateStockDto.getAmountToUpdate()
			);
		}

		productRepository.save(product);
		auditService.record(
			"UPDATE_STOCK",
			AUDIT_ENTITY,
			productCode,
			before,
			convertToResponseDto(product)
		);
	}

	/**
	 * Updates the name of a product without affecting other fields.
	 *
	 * @param productCode The product code of the product
	 * @param newProductName The new name for the product
	 */
	@Transactional
	public void updateStockName(String productCode, String newProductName) {
		Product product = productRepository
			.findByProductCodeAndIsDeletedFalse(productCode)
			.orElseThrow(() ->
				new ResourceNotFoundException(
					SystemLanguage.getMessage("product_not_found")
				)
			);
		ProductResponseDto before = convertToResponseDto(product);
		product.setName(newProductName);
		productRepository.save(product);
		auditService.record(
			"UPDATE_NAME",
			AUDIT_ENTITY,
			productCode,
			before,
			convertToResponseDto(product)
		);
	}

	/**
	 * Clears the stock of a product by setting its quantity to zero.
	 *
	 * @param productCode The product code of the product
	 * @throws ResourceNotFoundException if the product is not found
	 */
	@Transactional
	public void clearStock(String productCode) {
		Product product = productRepository
			.findByProductCodeAndIsDeletedFalse(productCode)
			.orElseThrow(() ->
				new ResourceNotFoundException("Product not found")
			);
		ProductResponseDto before = convertToResponseDto(product);
		product.setQuantity(0);
		productRepository.save(product);
		auditService.record(
			"CLEAR_STOCK",
			AUDIT_ENTITY,
			productCode,
			before,
			convertToResponseDto(product)
		);
	}

	/**
	 * Updates the price of a product.
	 * Validates that the new price is greater than 1000; throws an exception otherwise.
	 *
	 * @param productCode The product code of the product
	 * @param newStockPrice The new price to set
	 * @throws ResourceNotFoundException if the product is not found or price is invalid (<= 1000)
	 */
	@Transactional
	public void updatePrice(String productCode, int newStockPrice) {
		Product product = productRepository
			.findByProductCodeAndIsDeletedFalse(productCode)
			.orElseThrow(() ->
				new ResourceNotFoundException("Product not found")
			);
		if (newStockPrice <= 1000) {
			throw new ResourceNotFoundException("Stock price is invalid");
		}

		ProductResponseDto before = convertToResponseDto(product);
		product.setPrice(BigDecimal.valueOf(newStockPrice));
		productRepository.save(product);
		auditService.record(
			"UPDATE_PRICE",
			AUDIT_ENTITY,
			productCode,
			before,
			convertToResponseDto(product)
		);
	}

	/**
	 * Deletes a product by soft-deleting it (setting isDeleted flag to true).
	 *
	 * @param productCode The product code of the product to delete
	 * @throws ResourceNotFoundException if the product is not found
	 */
	@Transactional
	public void delete(String productCode) {
		Product product = productRepository
			.findByProductCodeAndIsDeletedFalse(productCode)
			.orElseThrow(() ->
				new ResourceNotFoundException("Product not found")
			);
		ProductResponseDto before = convertToResponseDto(product);
		product.setIsDeleted(true);
		productRepository.save(product);
		auditService.record("DELETE", AUDIT_ENTITY, productCode, before, null);
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
