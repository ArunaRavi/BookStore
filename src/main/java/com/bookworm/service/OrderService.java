package com.bookworm.service;

import com.bookworm.dto.DTOs.*;
import com.bookworm.model.*;
import com.bookworm.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final BasketRepository basketRepository;
    private final PaymentRepository paymentRepository;
    private final GiftPointsRepository giftPointsRepository;
    private final ProductRepository productRepository;
    private final CatalogService catalogService;
    private final BasketService basketService;
    private final CheckoutService checkoutService;

    public OrderService(OrderRepository orderRepository,
                        BasketRepository basketRepository,
                        PaymentRepository paymentRepository,
                        GiftPointsRepository giftPointsRepository,
                        ProductRepository productRepository,
                        CatalogService catalogService,
                        BasketService basketService,
                        CheckoutService checkoutService) {
        this.orderRepository = orderRepository;
        this.basketRepository = basketRepository;
        this.paymentRepository = paymentRepository;
        this.giftPointsRepository = giftPointsRepository;
        this.productRepository = productRepository;
        this.catalogService = catalogService;
        this.basketService = basketService;
        this.checkoutService = checkoutService;
    }

    @Transactional
    public OrderDetailDto createOrderFromBasket(User user, CreateOrderRequest req) {
        Payment payment = paymentRepository.findByIdAndUserId(req.paymentTransactionId(), user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Payment record not found"));

        if (!"SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            throw new IllegalArgumentException("Payment has not been successfully verified.");
        }

        Basket basket = basketService.getOrCreateBasket(user);
        if (basket.getItems().isEmpty()) {
            throw new IllegalArgumentException("Basket is empty.");
        }
        if (basket.getSelectedDeliveryAddress() == null) {
            throw new IllegalArgumentException("Delivery address is required.");
        }

        Address addr = basket.getSelectedDeliveryAddress();

        Order order = new Order();
        order.setUser(user);
        order.setStatus("CONFIRMED");
        order.setOrderTimestamp(LocalDateTime.now());
        order.setCancellationDeadline(LocalDateTime.now().plusHours(48)); // 48-hr policy

        order.setRecipientName(addr.getRecipientName());
        order.setPhone(addr.getPhone());
        order.setStreet(addr.getStreet());
        order.setCity(addr.getCity());
        order.setState(addr.getState());
        order.setPostalCode(addr.getPostalCode());
        order.setCountry(addr.getCountry());

        order.setSubtotal(basket.getSubtotal());
        order.setDiscountAmount(basket.getDiscountAmount());
        order.setGiftPointsRedeemed(basket.getGiftPointsRedeemed());
        order.setTotalPaid(basket.getTotalAmount());
        order.setPaymentMethod(payment.getPaymentMethod());
        order.setPaymentTransactionId(payment.getId());

        order = orderRepository.save(order);

        for (BasketItem item : basket.getItems()) {
            OrderItem orderItem = new OrderItem(
                    order,
                    item.getProduct().getId(),
                    item.getProduct().getTitle(),
                    item.getProduct().getBrand().getName(),
                    item.getProduct().getCoverImageUrl(),
                    item.getQuantity(),
                    item.getUnitPrice()
            );
            order.getItems().add(orderItem);
        }

        // Deduct gift points if redeemed
        if (basket.getGiftPointsRedeemed() > 0) {
            giftPointsRepository.findByUserId(user.getId()).ifPresent(gp -> {
                gp.setAvailablePoints(Math.max(0, gp.getAvailablePoints() - basket.getGiftPointsRedeemed()));
                giftPointsRepository.save(gp);
            });
        }

        order = orderRepository.save(order);

        // Clear user basket
        basket.getItems().clear();
        basket.setGiftPointsRedeemed(0);
        basket.setDiscountAmount(java.math.BigDecimal.ZERO);
        basketRepository.save(basket);

        return mapToOrderDetailDto(order);
    }

    public OrderDetailDto getOrderById(User user, String orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));
        return mapToOrderDetailDto(order);
    }

    public OrderHistoryResponse getOrderHistory(User user, int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);
        Page<Order> orderPage = orderRepository.findByUserIdOrderByOrderTimestampDesc(user.getId(), pageable);
        List<OrderDetailDto> list = orderPage.getContent().stream().map(this::mapToOrderDetailDto).toList();
        return new OrderHistoryResponse(list, orderPage.getTotalElements(), page, limit);
    }

    @Transactional
    public BasketResponse buyAgain(User user, String orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        for (OrderItem oi : order.getItems()) {
            basketService.addToBasket(user, new AddBasketItemRequest(oi.getProductId(), oi.getQuantity()));
        }

        return basketService.getBasketResponse(user);
    }

    public List<ProductSummaryDto> getHistoryBasedRecommendations(User user, int limit) {
        List<Order> orders = orderRepository.findByUserIdOrderByOrderTimestampDesc(user.getId());
        List<String> productIdsPurchased = orders.stream()
                .flatMap(o -> o.getItems().stream())
                .map(OrderItem::getProductId)
                .toList();

        List<Product> products = productRepository.findAllById(productIdsPurchased);
        List<String> categoryIds = products.stream()
                .map(p -> p.getCategory().getId())
                .distinct()
                .toList();

        Pageable pageable = PageRequest.of(0, limit);
        List<Product> recs;
        if (!categoryIds.isEmpty()) {
            recs = productRepository.findByCategoryIdIn(categoryIds, pageable);
        } else {
            recs = productRepository.findAll(pageable).getContent();
        }

        return recs.stream().map(catalogService::mapToSummaryDto).toList();
    }

    @Transactional
    public CancelOrderResponse cancelOrder(User user, String orderId, CancelOrderRequest req) {
        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        if (!order.isCancellable()) {
            throw new IllegalStateException("Order cannot be cancelled. The 48-hour cancellation period has expired or order is already cancelled/processed.");
        }

        order.setStatus("CANCELLED");
        order.setCancellationReason(req != null && req.reason() != null ? req.reason() : "Customer requested cancellation");
        order.setCancellationTimestamp(LocalDateTime.now());

        // Restore gift points
        if (order.getGiftPointsRedeemed() > 0) {
            giftPointsRepository.findByUserId(user.getId()).ifPresent(gp -> {
                gp.setAvailablePoints(gp.getAvailablePoints() + order.getGiftPointsRedeemed());
                giftPointsRepository.save(gp);
            });
        }

        orderRepository.save(order);

        return new CancelOrderResponse(
                order.getId(),
                order.getStatus(),
                order.getCancellationTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                order.getTotalPaid(),
                order.getGiftPointsRedeemed(),
                "Order successfully cancelled within 48-hour policy. Refund of " + order.getTotalPaid() + " initiated and points restored."
        );
    }

    public OrderDetailDto mapToOrderDetailDto(Order o) {
        List<OrderItemDto> itemDtos = o.getItems().stream().map(item -> new OrderItemDto(
                item.getProductId(),
                item.getTitle(),
                item.getBrandName(),
                item.getCoverImageUrl(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
        )).collect(Collectors.toList());

        AddressDto addrDto = new AddressDto(
                null,
                o.getRecipientName(),
                o.getPhone(),
                o.getStreet(),
                o.getCity(),
                o.getState(),
                o.getPostalCode(),
                o.getCountry(),
                null
        );

        return new OrderDetailDto(
                o.getId(),
                o.getOrderTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                o.getStatus(),
                itemDtos,
                addrDto,
                o.getSubtotal(),
                o.getDiscountAmount(),
                o.getGiftPointsRedeemed(),
                o.getTotalPaid(),
                o.getPaymentMethod(),
                o.getTentativeDeliveryDate(),
                o.getCancellationDeadline().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                o.isCancellable()
        );
    }
}
