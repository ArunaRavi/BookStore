package com.bookworm.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "basket_items")
public class BasketItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "basket_id", nullable = false)
    private Basket basket;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    public BasketItem() {}

    public BasketItem(Basket basket, Product product, Integer quantity, BigDecimal unitPrice) {
        this.basket = basket;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Basket getBasket() { return basket; }
    public void setBasket(Basket basket) { this.basket = basket; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getLineTotal() {
        return unitPrice != null && quantity != null 
                ? unitPrice.multiply(BigDecimal.valueOf(quantity)) 
                : BigDecimal.ZERO;
    }
}
