package com.bookworm.dto;

import java.math.BigDecimal;
import java.util.List;

public class DTOs {

    // --- Auth DTOs ---
    public record LoginRequest(String email, String password) {}
    public record RegisterRequest(String name, String email, String password, String phone) {}
    public record UserProfileResponse(String userId, String name, String email, String phone) {}
    public record AuthResponse(String accessToken, String tokenType, long expiresIn, UserProfileResponse user) {}

    // --- Catalog DTOs ---
    public record BrandDto(String brandId, String name, String type, String description, String avatarUrl) {}
    public record CategoryDto(String categoryId, String name, String slug, String description, Integer productCount) {}
    public record ProductSummaryDto(
            String productId,
            String title,
            BrandDto brand,
            String category,
            String synopsis,
            String format,
            BigDecimal price,
            String currency,
            String currencySymbol,
            String tentativeDeliveryDate,
            String coverImageUrl
    ) {}
    public record ProductDetailDto(
            String productId,
            String title,
            BrandDto brand,
            String category,
            String synopsis,
            String fullDescription,
            String format,
            BigDecimal price,
            String currency,
            String currencySymbol,
            String tentativeDeliveryDate,
            Integer tentativeDeliveryDays,
            String coverImageUrl,
            String isbn,
            String publisher,
            Integer pageCount,
            String language,
            Boolean inStock,
            Integer stockQuantity
    ) {}
    public record ProductListResponse(List<ProductSummaryDto> items, long total, int page, int limit, int totalPages) {}

    // --- Basket DTOs ---
    public record AddBasketItemRequest(String productId, Integer quantity) {}
    public record BasketItemDto(String itemId, ProductSummaryDto product, Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}
    public record BasketResponse(
            String basketId,
            int totalItemCount,
            List<BasketItemDto> items,
            BigDecimal subtotal,
            BigDecimal discountAmount,
            Integer giftPointsRedeemed,
            BigDecimal deliveryFee,
            BigDecimal totalAmount,
            String currency,
            String currencySymbol,
            AddressDto selectedDeliveryAddress
    ) {}

    // --- Address DTOs ---
    public record AddressRequest(String recipientName, String phone, String street, String city, String state, String postalCode, String country, Boolean isDefault) {}
    public record AddressDto(String addressId, String recipientName, String phone, String street, String city, String state, String postalCode, String country, Boolean isDefault) {}
    public record SetDeliveryAddressRequest(String addressId) {}

    // --- Loyalty / Points DTOs ---
    public record GiftPointsSummaryDto(Integer availablePoints, BigDecimal redemptionValueInINR, String conversionRate, Integer maxRedeemableForBasket) {}
    public record RedeemPointsRequest(Integer pointsToRedeem) {}

    // --- Payment DTOs ---
    public record InitiatePaymentRequest(String paymentMethod, String returnUrl) {}
    public record PaymentInitiateResponse(String paymentTransactionId, BigDecimal orderAmount, String currency, String paymentMethod, Object paymentGatewayPayload) {}
    public record VerifyPaymentRequest(String paymentTransactionId, String gatewayPaymentId, String gatewaySignature) {}
    public record PaymentVerificationResponse(String status, String paymentTransactionId, String orderId, String message) {}

    // --- Order DTOs ---
    public record CreateOrderRequest(String paymentTransactionId) {}
    public record OrderItemDto(String productId, String title, String brandName, String coverImageUrl, Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}
    public record OrderDetailDto(
            String orderId,
            String orderTimestamp,
            String status,
            List<OrderItemDto> items,
            AddressDto deliveryAddress,
            BigDecimal subtotal,
            BigDecimal discountAmount,
            Integer giftPointsRedeemed,
            BigDecimal totalPaid,
            String paymentMethod,
            String tentativeDeliveryDate,
            String cancellationDeadline,
            boolean isCancellable
    ) {}
    public record OrderHistoryResponse(List<OrderDetailDto> orders, long total, int page, int limit) {}
    public record CancelOrderRequest(String reason) {}
    public record CancelOrderResponse(String orderId, String status, String cancellationTimestamp, BigDecimal refundAmount, Integer pointsRestored, String message) {}

    // --- Error DTO ---
    public record ErrorResponse(String code, String message) {}
}
