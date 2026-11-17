package com.bookworm.controller;

import com.bookworm.dto.DTOs.*;
import com.bookworm.model.User;
import com.bookworm.service.BasketService;
import com.bookworm.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Recommendations & History", description = "Order history, Buy In Again, and history-driven recommendations (Step 2)")
public class RecommendationController {

    private final OrderService orderService;

    public RecommendationController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders/history")
    @Operation(summary = "Browse past order history")
    public ResponseEntity<OrderHistoryResponse> getOrderHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(orderService.getOrderHistory(user, page, limit));
    }

    @PostMapping("/orders/{orderId}/buy-again")
    @Operation(summary = "Buy In Again: Re-add past order items to basket")
    public ResponseEntity<BasketResponse> buyAgain(
            @AuthenticationPrincipal User user,
            @PathVariable String orderId) {
        return ResponseEntity.ok(orderService.buyAgain(user, orderId));
    }

    @GetMapping("/recommendations/history-based")
    @Operation(summary = "Recommended items based on order history")
    public ResponseEntity<List<ProductSummaryDto>> getHistoryBasedRecommendations(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(orderService.getHistoryBasedRecommendations(user, limit));
    }
}
