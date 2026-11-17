package com.bookworm.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "order_timestamp", nullable = false)
    private LocalDateTime orderTimestamp = LocalDateTime.now();

    @Column(nullable = false)
    private String status = "CONFIRMED"; // PLACED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "gift_points_redeemed")
    private Integer giftPointsRedeemed = 0;

    @Column(name = "total_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPaid;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "payment_transaction_id")
    private String paymentTransactionId;

    @Column(name = "tentative_delivery_date")
    private String tentativeDeliveryDate = "Mon, 21 Jul";

    @Column(name = "cancellation_deadline", nullable = false)
    private LocalDateTime cancellationDeadline;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "cancellation_timestamp")
    private LocalDateTime cancellationTimestamp;

    public Order() {}

    @PrePersist
    public void prePersist() {
        if (orderTimestamp == null) {
            orderTimestamp = LocalDateTime.now();
        }
        if (cancellationDeadline == null) {
            cancellationDeadline = orderTimestamp.plusHours(48); // 48 hrs cancellation policy
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public LocalDateTime getOrderTimestamp() { return orderTimestamp; }
    public void setOrderTimestamp(LocalDateTime orderTimestamp) { this.orderTimestamp = orderTimestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public Integer getGiftPointsRedeemed() { return giftPointsRedeemed; }
    public void setGiftPointsRedeemed(Integer giftPointsRedeemed) { this.giftPointsRedeemed = giftPointsRedeemed; }

    public BigDecimal getTotalPaid() { return totalPaid; }
    public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentTransactionId() { return paymentTransactionId; }
    public void setPaymentTransactionId(String paymentTransactionId) { this.paymentTransactionId = paymentTransactionId; }

    public String getTentativeDeliveryDate() { return tentativeDeliveryDate; }
    public void setTentativeDeliveryDate(String tentativeDeliveryDate) { this.tentativeDeliveryDate = tentativeDeliveryDate; }

    public LocalDateTime getCancellationDeadline() { return cancellationDeadline; }
    public void setCancellationDeadline(LocalDateTime cancellationDeadline) { this.cancellationDeadline = cancellationDeadline; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public LocalDateTime getCancellationTimestamp() { return cancellationTimestamp; }
    public void setCancellationTimestamp(LocalDateTime cancellationTimestamp) { this.cancellationTimestamp = cancellationTimestamp; }

    public boolean isCancellable() {
        return "CONFIRMED".equalsIgnoreCase(status) 
                && LocalDateTime.now().isBefore(cancellationDeadline);
    }
}
