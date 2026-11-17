package com.bookworm.controller;

import com.bookworm.dto.DTOs.*;
import com.bookworm.model.User;
import com.bookworm.service.CheckoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Addresses & Loyalty", description = "Address management and gift points balance (Steps 9 & 10)")
public class AddressAndLoyaltyController {

    private final CheckoutService checkoutService;

    public AddressAndLoyaltyController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @GetMapping("/addresses")
    @Operation(summary = "List user saved addresses (Step 9)")
    public ResponseEntity<List<AddressDto>> getAddresses(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(checkoutService.getUserAddresses(user));
    }

    @PostMapping("/addresses")
    @Operation(summary = "Add a new delivery address (Step 9)")
    public ResponseEntity<AddressDto> createAddress(
            @AuthenticationPrincipal User user,
            @RequestBody AddressRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(checkoutService.createAddress(user, req));
    }

    @GetMapping("/loyalty/gift-points")
    @Operation(summary = "Check available gift points balance (Step 10)")
    public ResponseEntity<GiftPointsSummaryDto> getGiftPoints(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(checkoutService.getGiftPointsSummary(user));
    }
}
