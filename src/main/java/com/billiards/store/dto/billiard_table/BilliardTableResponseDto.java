package com.billiards.store.dto.billiard_table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.billiards.store.common.enumerated.TableStatus;
import com.billiards.store.common.enumerated.TableType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BilliardTableResponseDto {
    private String tableCode;
    private TableType tableType;
    private TableStatus tableStatus;
    private BigDecimal pricePerHour;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
