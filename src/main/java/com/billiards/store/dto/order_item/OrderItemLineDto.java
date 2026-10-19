package com.billiards.store.dto.order_item;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "One product and quantity in an order")
public class OrderItemLineDto {
    @Schema(description = "Code of the product to order", example = "PRD000001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productCode;
    @Schema(description = "Units to order, greater than 0", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private int quantity;
}
