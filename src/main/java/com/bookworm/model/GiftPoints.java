package com.bookworm.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "gift_points")
public class GiftPoints {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "available_points", nullable = false)
    private Integer availablePoints = 100; // Sign-up bonus points

    @Column(name = "conversion_rate", nullable = false)
    private String conversionRate = "1 Point = ₹1.00";

    @Column(name = "point_value_in_inr", nullable = false, precision = 10, scale = 2)
    private BigDecimal pointValueInInr = BigDecimal.ONE;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public GiftPoints() {}

    public GiftPoints(User user, Integer availablePoints) {
        this.user = user;
        this.availablePoints = availablePoints;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getAvailablePoints() { return availablePoints; }
    public void setAvailablePoints(Integer availablePoints) { this.availablePoints = availablePoints; }

    public String getConversionRate() { return conversionRate; }
    public void setConversionRate(String conversionRate) { this.conversionRate = conversionRate; }

    public BigDecimal getPointValueInInr() { return pointValueInInr; }
    public void setPointValueInInr(BigDecimal pointValueInInr) { this.pointValueInInr = pointValueInInr; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public BigDecimal getRedemptionValueInINR() {
        return pointValueInInr.multiply(BigDecimal.valueOf(availablePoints != null ? availablePoints : 0));
    }
}
