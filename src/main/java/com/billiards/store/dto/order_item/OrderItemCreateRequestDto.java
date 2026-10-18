package com.billiards.store.dto.order_item;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemCreateRequestDto {
    private String sessionCode;
    private List<OrderItemLineDto> items;
}
