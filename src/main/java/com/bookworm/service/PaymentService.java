package com.bookworm.service;

import com.bookworm.dto.DTOs.*;
import com.bookworm.model.Basket;
import com.bookworm.model.Payment;
import com.bookworm.model.User;
import com.bookworm.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BasketService basketService;

    public PaymentService(PaymentRepository paymentRepository, BasketService basketService) {
        this.paymentRepository = paymentRepository;
        this.basketService = basketService;
    }

    @Transactional
    public PaymentInitiateResponse initiatePayment(User user, InitiatePaymentRequest req) {
        Basket basket = basketService.getOrCreateBasket(user);
        if (basket.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot initiate payment for an empty basket.");
        }
        if (basket.getSelectedDeliveryAddress() == null) {
            throw new IllegalArgumentException("Delivery address must be selected before initiating payment.");
        }

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setAmount(basket.getTotalAmount());
        payment.setCurrency(basket.getCurrency());
        payment.setPaymentMethod(req.paymentMethod());
        payment.setStatus("PENDING");
        payment.setGatewayOrderId("gateway_ord_" + UUID.randomUUID().toString().substring(0, 8));

        payment = paymentRepository.save(payment);

        Object payload = Map.of(
                "gatewayOrderId", payment.getGatewayOrderId(),
                "paymentMethod", req.paymentMethod(),
                "amount", payment.getAmount(),
                "upiIntentUrl", "upi://pay?pa=bookworm@bank&pn=BookWorm&am=" + payment.getAmount()
        );

        return new PaymentInitiateResponse(payment.getId(), payment.getAmount(), payment.getCurrency(), payment.getPaymentMethod(), payload);
    }

    @Transactional
    public PaymentVerificationResponse verifyPayment(User user, VerifyPaymentRequest req) {
        Payment payment = paymentRepository.findByIdAndUserId(req.paymentTransactionId(), user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found"));

        // Simulate verification logic
        payment.setStatus("SUCCESS");
        payment.setGatewayPaymentId(req.gatewayPaymentId() != null ? req.gatewayPaymentId() : "pay_" + UUID.randomUUID().toString().substring(0, 8));
        payment.setGatewaySignature(req.gatewaySignature() != null ? req.gatewaySignature() : "sig_" + UUID.randomUUID().toString().substring(0, 8));
        paymentRepository.save(payment);

        return new PaymentVerificationResponse("SUCCESS", payment.getId(), null, "Payment verified successfully.");
    }
}
