package com.billiards.store.dto.billiard_table;

import java.math.BigDecimal;

import com.billiards.store.common.enumerated.TableStatus;
import com.billiards.store.common.enumerated.TableType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data for creating a table")
public class BilliardTableRequestDto {
    @Schema(description = "Kind of table", requiredMode = Schema.RequiredMode.REQUIRED)
    private TableType tableType;
    @Schema(description = "Initial status, normally AVAILABLE", requiredMode = Schema.RequiredMode.REQUIRED)
    private TableStatus tableStatus;
    @Schema(description = "Rental price per hour in VND, must be greater than 0", example = "60000", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal pricePerHour;
}
