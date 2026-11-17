package com.bookworm.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "baskets")
public class Basket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "basket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BasketItem> items = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "delivery_address_id")
    private Address selectedDeliveryAddress;

    @Column(name = "gift_points_redeemed")
    private Integer giftPointsRedeemed = 0;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(nullable = false)
    private String currency = "INR";

    @Column(name = "currency_symbol", nullable = false)
    private String currencySymbol = "₹";

    public Basket() {}

    public Basket(User user) {
        this.user = user;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<BasketItem> getItems() { return items; }
    public void setItems(List<BasketItem> items) { this.items = items; }

    public Address getSelectedDeliveryAddress() { return selectedDeliveryAddress; }
    public void setSelectedDeliveryAddress(Address selectedDeliveryAddress) { this.selectedDeliveryAddress = selectedDeliveryAddress; }

    public Integer getGiftPointsRedeemed() { return giftPointsRedeemed; }
    public void setGiftPointsRedeemed(Integer giftPointsRedeemed) { this.giftPointsRedeemed = giftPointsRedeemed; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(BigDecimal deliveryFee) { this.deliveryFee = deliveryFee; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }

    public BigDecimal getSubtotal() {
        return items.stream()
                .map(BasketItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalAmount() {
        BigDecimal total = getSubtotal().subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO)
                .add(deliveryFee != null ? deliveryFee : BigDecimal.ZERO);
        return total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total;
    }

    public int getTotalItemCount() {
        return items.stream().mapToInt(BasketItem::getQuantity).sum();
    }
}
