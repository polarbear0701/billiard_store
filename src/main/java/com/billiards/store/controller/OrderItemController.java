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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@RequestMapping("/order-item")
public class OrderItemController {
    private final OrderItemService orderItemService;

    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }


    @PostMapping("/create")
    public ResponseEntity<SuccessResponse<?>> create(@RequestBody OrderItemRequestDto request) {
        OrderItemResponseDto responseDto = orderItemService.create(request);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.ACCEPTED.value(),
                "success",
                responseDto,
                LocalDateTime.now()
        ));
    }

    @PostMapping("/cancel/{orderItemCode}")
    public ResponseEntity<SuccessResponse<?>> cancelOrder(@PathVariable String orderItemCode) {
        orderItemService.cancelOrder(orderItemCode);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Order cancelled successfully",
                null,
                LocalDateTime.now()
        ));
    }

    @GetMapping("/all")
    public ResponseEntity<SuccessResponse<?>> findAll() {
        List<OrderItemResponseDto> itemsDto = orderItemService.findAll();
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "success",
                itemsDto,
                LocalDateTime.now()
        ));
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
    public ResponseEntity<SuccessResponse<?>> findByOrderItemCode(@PathVariable String orderItemCode) {
        OrderItemResponseDto itemDto = orderItemService.findByOrderItemCode(orderItemCode);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "success",
                itemDto,
                LocalDateTime.now()
        ));
    }

    @GetMapping("/all/{sessionCode}")
    public ResponseEntity<SuccessResponse<?>> getAllOrderFromSessionCode(@PathVariable String sessionCode) {
        List<OrderItemResponseDto> itemsDto = orderItemService.findOrderItemsBySessionCode(sessionCode);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "success",
                itemsDto,
                LocalDateTime.now()
        ));
    }

    @PutMapping("/update/quantity")
    public ResponseEntity<SuccessResponse<?>> updateOrderItemQuantity(@RequestParam String orderItemCode, @RequestParam int newQuantity) {
        orderItemService.updateOrderQuantity(orderItemCode, newQuantity);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "success",
                null,
                LocalDateTime.now()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<?>> delete(@PathVariable UUID id) {
        orderItemService.delete(id);
        return ResponseEntity.ok(new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Order item deleted successfully",
                null,
                LocalDateTime.now()
        ));
    }
}

