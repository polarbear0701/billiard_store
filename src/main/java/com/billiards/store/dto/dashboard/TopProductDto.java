package com.billiards.store.dto.dashboard;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "A best-selling product")
public class TopProductDto {
    @Schema(description = "Product code", example = "PRD000001")
    private String productCode;
    @Schema(description = "Product name", example = "Coca-Cola can")
    private String name;
    @Schema(description = "Units sold in the period", example = "120")
    private long quantitySold;
    @Schema(description = "Revenue from this product in VND", example = "1800000.00")
    private BigDecimal revenue;
}
