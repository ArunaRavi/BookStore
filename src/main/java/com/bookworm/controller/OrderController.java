package com.bookworm.controller;

import com.bookworm.dto.DTOs.*;
import com.bookworm.model.User;
import com.bookworm.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Orders & Cancellation", description = "Order placement, tracking, and 48-hour cancellation policy (Steps 11 & 12)")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Create order upon payment confirmation (Step 11, 12)")
    public ResponseEntity<OrderDetailDto> createOrder(
            @AuthenticationPrincipal User user,
            @RequestBody CreateOrderRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrderFromBasket(user, req));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details and confirmation status (Step 12)")
    public ResponseEntity<OrderDetailDto> getOrderById(
            @AuthenticationPrincipal User user,
            @PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrderById(user, orderId));
    }

    @PostMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel order within 48 hours (Step 12)")
    public ResponseEntity<CancelOrderResponse> cancelOrder(
            @AuthenticationPrincipal User user,
            @PathVariable String orderId,
            @RequestBody(required = false) CancelOrderRequest req) {
        return ResponseEntity.ok(orderService.cancelOrder(user, orderId, req));
    }
}
