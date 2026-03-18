package com.billiards.store.dto.billiard_table;

import java.math.BigDecimal;

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
public class BilliardTableRequestDto {
    private TableType tableType;
    private TableStatus tableStatus;
    private BigDecimal pricePerHour;
}
