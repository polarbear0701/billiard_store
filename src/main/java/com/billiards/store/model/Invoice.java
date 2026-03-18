package com.billiards.store.model;

import java.math.BigDecimal;

import com.billiards.store.common.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invoice")
public class Invoice extends BaseEntity {
	@Column(unique = true, nullable = false, updatable = false)
    private String invoiceCode;
    
    private String sessionCode;
    
    private BigDecimal tableFee;
    
    private BigDecimal fnbFee;
    
    private BigDecimal totalAmount;
}
