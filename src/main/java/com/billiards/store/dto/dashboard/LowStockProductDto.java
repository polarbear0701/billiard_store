package com.billiards.store.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "A product with low stock")
public class LowStockProductDto {
    @Schema(description = "Product code", example = "PRD000001")
    private String productCode;
    @Schema(description = "Product name", example = "Coca-Cola can")
    private String name;
    @Schema(description = "Units left in stock", example = "3")
    private int quantity;
}
