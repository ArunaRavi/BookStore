package com.bookworm.controller;

import com.bookworm.dto.DTOs.*;
import com.bookworm.model.User;
import com.bookworm.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Payments", description = "Initiate and verify payments (Steps 10 & 11)")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/initiate")
    @Operation(summary = "Initiate payment with selected option (Step 10)")
    public ResponseEntity<PaymentInitiateResponse> initiatePayment(
            @AuthenticationPrincipal User user,
            @RequestBody InitiatePaymentRequest req) {
        return ResponseEntity.ok(paymentService.initiatePayment(user, req));
    }

    @PostMapping("/verify")
    @Operation(summary = "Payment confirmation and transaction verification (Step 11)")
    public ResponseEntity<PaymentVerificationResponse> verifyPayment(
            @AuthenticationPrincipal User user,
            @RequestBody VerifyPaymentRequest req) {
        return ResponseEntity.ok(paymentService.verifyPayment(user, req));
    }
}
