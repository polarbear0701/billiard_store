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
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "product")
public class Product extends BaseEntity {
	@Column(unique = true, nullable = false, updatable = false)
    private String productCode;
    
    private String name;
    
    private int quantity;
    
    private BigDecimal price;

    /** Buy-in price per unit; null when not known yet. */
    private BigDecimal costPrice;
}
