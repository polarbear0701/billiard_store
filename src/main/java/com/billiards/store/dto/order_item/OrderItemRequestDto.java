package com.billiards.store.dto.order_item;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequestDto {
    private String sessionCode;
    private String productCode;
    private int quantity;
    private BigDecimal price;
}
