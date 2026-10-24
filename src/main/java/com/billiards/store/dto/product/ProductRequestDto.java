package com.billiards.store.dto.product;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data for creating a product")
public class ProductRequestDto {
    @Schema(description = "Product name", example = "Coca-Cola can", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @Schema(description = "Units in stock", example = "24", requiredMode = Schema.RequiredMode.REQUIRED)
    private int quantity;
    @Schema(description = "Unit price in VND", example = "15000", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;
    @Schema(description = "Buy-in price per unit in VND, 0 or more. Optional: leave out if not known yet. When updating, leaving it out keeps the current cost", example = "10000")
    private BigDecimal costPrice;
}
