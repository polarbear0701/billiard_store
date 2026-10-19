package com.billiards.store.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LowStockProductDto {
    private String productCode;
    private String name;
    private int quantity;
}
