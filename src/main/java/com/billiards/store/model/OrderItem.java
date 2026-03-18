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
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "order_item")
public class OrderItem extends BaseEntity {
	@Column(unique = true, nullable = false, updatable = false)
    private String orderItemCode;
    
    private String sessionCode;
    
    private String productCode;
    
    private int quantity;
    
    private BigDecimal price;
}
