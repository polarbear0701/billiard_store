package com.billiards.store.repository;

import com.billiards.store.model.Invoice;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for Invoice entity operations.
 * Provides methods for finding invoices and checking existence.
 */
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
	/**
	 * Finds an invoice by its code if it exists and is not deleted.
	 * @param invoiceCode The unique invoice code
	 * @return Optional containing the invoice if found and not deleted, or empty otherwise
	 */
	Optional<Invoice> findByInvoiceCodeAndIsDeletedFalse(String invoiceCode);

	/**
	 * Finds an invoice by its ID if it exists and is not deleted.
	 * @param id The unique invoice identifier
	 * @return Optional containing the invoice if found and not deleted, or empty otherwise
	 */
	Optional<Invoice> findByIdAndIsDeletedFalse(UUID id);

	/**
	 * Returns all invoices that are not marked as deleted.
	 * @return List of all non-deleted invoices
	 */
	List<Invoice> findByIsDeletedFalse();

	/**
	 * Returns a page of invoices that are not marked as deleted.
	 * @param pageable the pagination and sorting information
	 * @return a page of non-deleted invoices
	 */
	Page<Invoice> findByIsDeletedFalse(Pageable pageable);

	/**
	 * Checks if an invoice exists for the given session code and is not deleted.
	 * @param sessionCode The session code to check
	 * @return true if an invoice exists for this session, false otherwise
	 */
	boolean existsBySessionCodeAndIsDeletedFalse(String sessionCode);

	/**
	 * Finds the total amount for an invoice by its code if it exists and is not deleted.
	 * @param invoiceCode The invoice code to search for
	 * @return Optional containing the total amount if found and not deleted, or empty otherwise
	 */
	@Query(
		"SELECT i.totalAmount FROM Invoice i WHERE i.invoiceCode = :invoiceCode AND i.isDeleted = false"
	)
	Optional<BigDecimal> findTotalAmountByInvoiceCode(
		@Param("invoiceCode") String invoiceCode
	);
}
