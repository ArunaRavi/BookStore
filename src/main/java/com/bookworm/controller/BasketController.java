package com.bookworm.controller;

import com.bookworm.dto.DTOs.*;
import com.bookworm.model.User;
import com.bookworm.service.BasketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/basket")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Basket / Cart", description = "Basket operations and item management (Step 8)")
public class BasketController {

    private final BasketService basketService;

    public BasketController(BasketService basketService) {
        this.basketService = basketService;
    }

    @GetMapping
    @Operation(summary = "Get active basket")
    public ResponseEntity<BasketResponse> getBasket(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(basketService.getBasketResponse(user));
    }

    @PostMapping("/items")
    @Operation(summary = "Add product(s) to basket")
    public ResponseEntity<BasketResponse> addToBasket(
            @AuthenticationPrincipal User user,
            @RequestBody AddBasketItemRequest req) {
        return ResponseEntity.ok(basketService.addToBasket(user, req));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update basket item quantity")
    public ResponseEntity<BasketResponse> updateItem(
            @AuthenticationPrincipal User user,
            @PathVariable String itemId,
            @RequestBody AddBasketItemRequest req) {
        return ResponseEntity.ok(basketService.updateItemQuantity(user, itemId, req.quantity()));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from basket")
    public ResponseEntity<BasketResponse> removeItem(
            @AuthenticationPrincipal User user,
            @PathVariable String itemId) {
        return ResponseEntity.ok(basketService.removeItem(user, itemId));
    }

    @PutMapping("/delivery-address")
    @Operation(summary = "Select address for order delivery (Step 9)")
    public ResponseEntity<BasketResponse> setDeliveryAddress(
            @AuthenticationPrincipal User user,
            @RequestBody SetDeliveryAddressRequest req) {
        return ResponseEntity.ok(basketService.setDeliveryAddress(user, req.addressId()));
    }

    @PostMapping("/redeem-points")
    @Operation(summary = "Redeem gift points for discount (Step 10)")
    public ResponseEntity<BasketResponse> redeemPoints(
            @AuthenticationPrincipal User user,
            @RequestBody RedeemPointsRequest req) {
        return ResponseEntity.ok(basketService.redeemPoints(user, req.pointsToRedeem()));
    }
}
