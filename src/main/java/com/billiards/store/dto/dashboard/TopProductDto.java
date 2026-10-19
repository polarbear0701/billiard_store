package com.billiards.store.dto.dashboard;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TopProductDto {
    private String productCode;
    private String name;
    private long quantitySold;
    private BigDecimal revenue;
}
