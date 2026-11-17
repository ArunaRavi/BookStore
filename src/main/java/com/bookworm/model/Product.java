package com.bookworm.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(columnDefinition = "TEXT")
    private String synopsis;

    @Column(name = "full_description", columnDefinition = "TEXT")
    private String fullDescription;

    @Column(nullable = false)
    private String format = "Paperback"; // Paperback, Hardcover, eBook, Audiobook

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String currency = "INR";

    @Column(name = "currency_symbol", nullable = false)
    private String currencySymbol = "₹";

    @Column(name = "tentative_delivery_date")
    private String tentativeDeliveryDate = "Mon, 21 Jul";

    @Column(name = "tentative_delivery_days")
    private Integer tentativeDeliveryDays = 3;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    private String isbn;
    private String publisher;

    @Column(name = "page_count")
    private Integer pageCount;

    private String language = "English";

    @Column(name = "in_stock")
    private Boolean inStock = true;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 50;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Product() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Brand getBrand() { return brand; }
    public void setBrand(Brand brand) { this.brand = brand; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getSynopsis() { return synopsis; }
    public void setSynopsis(String synopsis) { this.synopsis = synopsis; }

    public String getFullDescription() { return fullDescription; }
    public void setFullDescription(String fullDescription) { this.fullDescription = fullDescription; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getCurrencySymbol() { return currencySymbol; }
    public void setCurrencySymbol(String currencySymbol) { this.currencySymbol = currencySymbol; }

    public String getTentativeDeliveryDate() { return tentativeDeliveryDate; }
    public void setTentativeDeliveryDate(String tentativeDeliveryDate) { this.tentativeDeliveryDate = tentativeDeliveryDate; }

    public Integer getTentativeDeliveryDays() { return tentativeDeliveryDays; }
    public void setTentativeDeliveryDays(Integer tentativeDeliveryDays) { this.tentativeDeliveryDays = tentativeDeliveryDays; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Boolean getInStock() { return inStock; }
    public void setInStock(Boolean inStock) { this.inStock = inStock; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
