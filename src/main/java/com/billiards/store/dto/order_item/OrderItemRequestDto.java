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
@Schema(description = "A single order line")
public class OrderItemRequestDto {
    @Schema(description = "Play session code", example = "SES000001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String sessionCode;
    @Schema(description = "Product code", example = "PRD000001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String productCode;
    @Schema(description = "Units ordered", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private int quantity;
}
