package com.billiards.store.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import com.billiards.store.common.base.SuccessResponse;
import com.billiards.store.dto.order_item.OrderItemRequestDto;
import com.billiards.store.dto.order_item.OrderItemResponseDto;
import com.billiards.store.service.OrderItemService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/order-item")
public class OrderItemController {
    private final OrderItemService orderItemService;

    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }


    @PostMapping("/create")
    public OrderItemResponseDto create(@RequestBody OrderItemRequestDto request) {
        return orderItemService.create(request);
    }

    @GetMapping
    public List<OrderItemResponseDto> findAll() {
        return orderItemService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<?>> findById(@PathVariable UUID id) {
        OrderItemResponseDto responseDto = orderItemService.findById(id);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "success",
                responseDto,
                LocalDateTime.now()

        ));
    }

    @GetMapping("/order-item-code/{orderItemCode}")
    public OrderItemResponseDto findByOrderItemCode(@PathVariable String orderItemCode) {
        return orderItemService.findByOrderItemCode(orderItemCode);
    }

    @PutMapping("/{id}")
    public OrderItemResponseDto update(@PathVariable UUID id, @RequestBody OrderItemRequestDto request) {
        return orderItemService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        orderItemService.delete(id);
    }
}

