package com.billiards.store.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.billiards.store.common.enumerated.SessionStatus;
import com.billiards.store.common.exception.ResourceNotFoundException;
import com.billiards.store.common.util.CodeGeneratorUtil;
import com.billiards.store.dto.order_item.OrderItemRequestDto;
import com.billiards.store.dto.order_item.OrderItemResponseDto;
import com.billiards.store.dto.product.ProductResponseDto;
import com.billiards.store.dto.product.ProductUpdateStockDto;
import com.billiards.store.model.OrderItem;
import com.billiards.store.repository.OrderItemRepository;

import jakarta.transaction.Transactional;

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

    @Transactional
    public OrderItemResponseDto create(OrderItemRequestDto requestDto) {

        validateRequestDto(
            requestDto
        );

        ProductResponseDto productResponseDto = productService.findByProductCode(requestDto.getProductCode());

        playSessionService.findBySessionCode(requestDto.getSessionCode());


        OrderItem orderItem = new OrderItem();
        orderItem.setOrderItemCode(
            codeGeneratorUtil.generateCode("ORI", "order_item_code_seq")
        );

        orderItem.setSessionCode(requestDto.getSessionCode());
        orderItem.setProductCode(requestDto.getProductCode());
        orderItem.setQuantity(requestDto.getQuantity());

        orderItem.setPrice(productResponseDto.getPrice());
        ProductUpdateStockDto productUpdateStockDto = new ProductUpdateStockDto(true, requestDto.getQuantity());

        productService.updateStock(requestDto.getProductCode(), productUpdateStockDto);
        
        OrderItem savedOrderItem = orderItemRepository.save(orderItem);
        OrderItemResponseDto orderItemResponseDto =  convertToResponseDto(savedOrderItem);
        orderItemResponseDto.setProductName(productResponseDto.getName());

        return orderItemResponseDto;
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

    public BigDecimal calculateOrderItemFnbFee(String orderItemCode) {
        OrderItem orderItem = orderItemRepository.findByOrderItemCodeAndIsDeletedFalse(orderItemCode)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
        BigDecimal unitPrice = orderItem.getPrice();
        int quantity = orderItem.getQuantity();
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal calculateTotalFnbFee(String playSessionCode) {
        List<OrderItemResponseDto> allOrders = findOrderItemsBySessionCode(playSessionCode);

        BigDecimal total = new BigDecimal(0);

        for (OrderItemResponseDto order: allOrders) {
            total = total.add(
                order.getPrice().multiply(BigDecimal.valueOf(
                    order.getQuantity()
                ))
            );
        }

        return total;
                
    }

    @Transactional
    public void cancelOrder(String orderItemCode) {
        OrderItem order = orderItemRepository.findByOrderItemCodeAndIsDeletedFalse(orderItemCode)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
        
        if (playSessionService.checkCurrentSessionStatus(order.getSessionCode()) == SessionStatus.ACTIVE){
            ProductUpdateStockDto productUpdateStockDto = new ProductUpdateStockDto(false, order.getQuantity());
            productService.updateStock(order.getProductCode(), productUpdateStockDto);
            order.setIsDeleted(true);
            orderItemRepository.save(order);
        } else {
            throw new ResourceNotFoundException("Cannot cancel this order, as session has been completed");
        }
    }

    @Transactional
    public void updateOrderQuantity(String orderItemCode, int newQuantity) {
        OrderItem order = orderItemRepository.findByOrderItemCodeAndIsDeletedFalse(orderItemCode)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
        
        if (newQuantity <= 0 || newQuantity == order.getQuantity()) {
            throw new ResourceNotFoundException("Invalid new quantity");
        }
        if (playSessionService.checkCurrentSessionStatus(order.getSessionCode()) == SessionStatus.ACTIVE){
            if (newQuantity < order.getQuantity()) {
                ProductUpdateStockDto productUpdateStockDto = new ProductUpdateStockDto(false, order.getQuantity() - newQuantity);
                productService.updateStock(order.getProductCode(), productUpdateStockDto);
            } else {
                ProductUpdateStockDto productUpdateStockDto = new ProductUpdateStockDto(true, newQuantity -  order.getQuantity() );
                productService.updateStock(order.getProductCode(), productUpdateStockDto);
            }
            order.setQuantity(newQuantity);
            orderItemRepository.save(order);
        } else {
            throw new ResourceNotFoundException("Cannot cancel this order, as session has been completed");
        }
    }

    @Transactional
    public OrderItemResponseDto update(UUID id, OrderItemRequestDto requestDto) {
        OrderItem orderItem = orderItemRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        orderItem.setSessionCode(requestDto.getSessionCode());
        orderItem.setProductCode(requestDto.getProductCode());
        orderItem.setQuantity(requestDto.getQuantity());



        orderItem.setPrice(productService.getProductPrice(requestDto.getProductCode()));
        
        OrderItem updatedOrderItem = orderItemRepository.save(orderItem);
        return convertToResponseDto(updatedOrderItem);
    }

    public List<OrderItemResponseDto> findOrderItemsBySessionCode(String sessionCode) {
        return orderItemRepository.findAllBySessionCodeAndIsDeletedFalse(sessionCode).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    } 

    public void delete(UUID id) {
        OrderItem orderItem = orderItemRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
        ProductUpdateStockDto productUpdateStockDto = new ProductUpdateStockDto(false, orderItem.getQuantity());
        productService.updateStock(orderItem.getProductCode(), productUpdateStockDto);
        orderItem.setIsDeleted(true);
        orderItemRepository.save(orderItem);
    }

    private void validateRequestDto(
        OrderItemRequestDto requestDto
    ) {
        if (requestDto.getSessionCode() == null || requestDto.getSessionCode().trim().isEmpty()) {
            throw new ResourceNotFoundException("Session code cannot be empty");
        }
        if (requestDto.getProductCode() == null || requestDto.getProductCode().trim().isEmpty()) {
            throw new ResourceNotFoundException("Product code cannot be empty");
        }
        if (requestDto.getQuantity() <= 0) {
            throw new ResourceNotFoundException("Quantity cannot be less than 0");
        }

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
