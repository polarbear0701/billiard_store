package com.billiards.store.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.billiards.store.common.exception.ResourceNotFoundException;
import com.billiards.store.dto.order_item.OrderItemRequestDto;
import com.billiards.store.dto.order_item.OrderItemResponseDto;
import com.billiards.store.model.OrderItem;
import com.billiards.store.repository.OrderItemRepository;
import com.billiards.store.util.CodeGeneratorUtil;

@Service
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final CodeGeneratorUtil codeGeneratorUtil;

    public OrderItemService(
        OrderItemRepository orderItemRepository,
        CodeGeneratorUtil codeGeneratorUtil
    ) {
        this.orderItemRepository = orderItemRepository;
        this.codeGeneratorUtil = codeGeneratorUtil;
    }

    public OrderItemResponseDto create(OrderItemRequestDto requestDto) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemCode(
            codeGeneratorUtil.generateCode("ORI", "order_item_code_seq")
        );
        orderItem.setSessionCode(requestDto.getSessionCode());
        orderItem.setProductCode(requestDto.getProductCode());
        orderItem.setQuantity(requestDto.getQuantity());
        orderItem.setPrice(requestDto.getPrice());
        
        OrderItem savedOrderItem = orderItemRepository.save(orderItem);
        return convertToResponseDto(savedOrderItem);
    }

    public OrderItemResponseDto findById(UUID id) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
        return convertToResponseDto(orderItem);
    }

    public OrderItemResponseDto findByOrderItemCode(String orderItemCode) {
        OrderItem orderItem = orderItemRepository.findByOrderItemCode(orderItemCode)
            .orElseThrow(() -> new ResourceNotFoundException("No order item associated with this order item code. Please try again"));
        return convertToResponseDto(orderItem);
    }

    public List<OrderItemResponseDto> findAll() {
        return orderItemRepository.findAll().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public OrderItemResponseDto update(UUID id, OrderItemRequestDto requestDto) {
        OrderItem orderItem = orderItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        orderItem.setSessionCode(requestDto.getSessionCode());
        orderItem.setProductCode(requestDto.getProductCode());
        orderItem.setQuantity(requestDto.getQuantity());
        orderItem.setPrice(requestDto.getPrice());
        
        OrderItem updatedOrderItem = orderItemRepository.save(orderItem);
        return convertToResponseDto(updatedOrderItem);
    }

    public void delete(UUID id) {
        orderItemRepository.deleteById(id);
    }

    private OrderItemResponseDto convertToResponseDto(OrderItem orderItem) {
        OrderItemResponseDto dto = new OrderItemResponseDto();
        dto.setOrderItemCode(orderItem.getOrderItemCode());
        dto.setSessionCode(orderItem.getSessionCode());
        dto.setProductCode(orderItem.getProductCode());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        dto.setCreatedAt(orderItem.getCreatedAt());
        dto.setUpdatedAt(orderItem.getUpdatedAt());
        dto.setCreatedBy(orderItem.getCreatedBy());
        dto.setUpdatedBy(orderItem.getUpdatedBy());
        return dto;
    }
}
