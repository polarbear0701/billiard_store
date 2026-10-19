package com.billiards.store.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Stock change for a product")
public class ProductUpdateStockDto {
    @Schema(description = "true to subtract from stock, false to add to it", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean decrement;
    @Schema(description = "Number of units to subtract or add, greater than 0", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private int amountToUpdate;
}
