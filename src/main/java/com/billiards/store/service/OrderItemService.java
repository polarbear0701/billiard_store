package com.billiards.store.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.billiards.store.common.exception.ResourceNotFoundException;
import com.billiards.store.common.util.CodeGeneratorUtil;
import com.billiards.store.dto.order_item.OrderItemRequestDto;
import com.billiards.store.dto.order_item.OrderItemResponseDto;
import com.billiards.store.model.OrderItem;
import com.billiards.store.repository.OrderItemRepository;

@Service
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;
    private final CodeGeneratorUtil codeGeneratorUtil;
    private final PlaySessionService playSessionService;
    private final ProductService productService;

    public OrderItemService(
        OrderItemRepository orderItemRepository,
        CodeGeneratorUtil codeGeneratorUtil,
        PlaySessionService playSessionService,
        ProductService productService
    ) {
        this.orderItemRepository = orderItemRepository;
        this.codeGeneratorUtil = codeGeneratorUtil;
        this.playSessionService = playSessionService;
        this.productService = productService;
    }

    public OrderItemResponseDto create(OrderItemRequestDto requestDto) {
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemCode(
            codeGeneratorUtil.generateCode("ORI", "order_item_code_seq")
        );

        validateReferenceCode(
            requestDto.getProductCode(),
            requestDto.getSessionCode()
        );

        orderItem.setSessionCode(requestDto.getSessionCode());
        orderItem.setProductCode(requestDto.getProductCode());
        orderItem.setQuantity(requestDto.getQuantity());
        orderItem.setPrice(requestDto.getPrice());
        
        OrderItem savedOrderItem = orderItemRepository.save(orderItem);
        return convertToResponseDto(savedOrderItem);
    }

    public OrderItemResponseDto findById(UUID id) {
        OrderItem orderItem = orderItemRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
        return convertToResponseDto(orderItem);
    }

    public OrderItemResponseDto findByOrderItemCode(String orderItemCode) {
        OrderItem orderItem = orderItemRepository.findByOrderItemCodeAndIsDeletedFalse(orderItemCode)
            .orElseThrow(() -> new ResourceNotFoundException("No order item associated with this order item code. Please try again"));
        return convertToResponseDto(orderItem);
    }

    public List<OrderItemResponseDto> findAll() {
        return orderItemRepository.findByIsDeletedFalse().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public OrderItemResponseDto update(UUID id, OrderItemRequestDto requestDto) {
        OrderItem orderItem = orderItemRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        orderItem.setSessionCode(requestDto.getSessionCode());
        orderItem.setProductCode(requestDto.getProductCode());
        orderItem.setQuantity(requestDto.getQuantity());
        orderItem.setPrice(requestDto.getPrice());
        
        OrderItem updatedOrderItem = orderItemRepository.save(orderItem);
        return convertToResponseDto(updatedOrderItem);
    }

    public void delete(UUID id) {
        OrderItem orderItem = orderItemRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
        orderItem.setIsDeleted(true);
        orderItemRepository.save(orderItem);
    }

    private void validateReferenceCode(
        String productCode,
        String sessionCode
    ) {
        if (sessionCode == null || sessionCode.trim().isEmpty()) {
            throw new ResourceNotFoundException("Session code cannot be empty");
        }
        if (productCode == null || productCode.trim().isEmpty()) {
            throw new ResourceNotFoundException("Product code cannot be empty");
        }
        playSessionService.findBySessionCode(sessionCode);
        productService.findByProductCode(sessionCode);
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
