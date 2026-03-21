package com.billiards.store.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billiards.store.model.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByInvoiceCodeAndIsDeletedFalse(String invoiceCode);
    Optional<Invoice> findByIdAndIsDeletedFalse(UUID id);
    List<Invoice> findByIsDeletedFalse();
    boolean existsBySessionCodeAndIsDeletedFalse(String sessionCode);
}
