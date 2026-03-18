package com.billiards.store.model;

import java.math.BigDecimal;

import com.billiards.store.common.base.BaseEntity;
import com.billiards.store.common.enumerated.TableStatus;
import com.billiards.store.common.enumerated.TableType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Table(name = "billiard_table")
public class BilliardTable extends BaseEntity {
    
    @Column(unique=true, nullable=false, updatable = false)
    private String tableCode;

    @Enumerated(EnumType.STRING)
    private TableType tableType;
    
    @Enumerated(EnumType.STRING)
    private TableStatus tableStatus;
    
    private BigDecimal pricePerHour;
}
