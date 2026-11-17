package com.bookworm.service;

import com.bookworm.dto.DTOs.*;
import com.bookworm.model.Brand;
import com.bookworm.model.Category;
import com.bookworm.model.Product;
import com.bookworm.repository.BrandRepository;
import com.bookworm.repository.CategoryRepository;
import com.bookworm.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    public CatalogService(CategoryRepository categoryRepository,
                          BrandRepository brandRepository,
                          ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
    }

    public List<CategoryDto> getCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToCategoryDto)
                .toList();
    }

    public List<BrandDto> getBrands(String search) {
        List<Brand> list = (search != null && !search.isBlank())
                ? brandRepository.findByNameContainingIgnoreCase(search)
                : brandRepository.findAll();
        return list.stream().map(this::mapToBrandDto).toList();
    }

    public ProductListResponse getProductsByBrand(String brandId, int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit);
        Page<Product> pageResult = productRepository.findByBrandId(brandId, pageable);
        List<ProductSummaryDto> items = pageResult.getContent().stream().map(this::mapToSummaryDto).toList();
        return new ProductListResponse(items, pageResult.getTotalElements(), page, limit, pageResult.getTotalPages());
    }

    public ProductListResponse searchProducts(String search, String categoryId, String brandId,
                                              BigDecimal minPrice, BigDecimal maxPrice,
                                              String language,
                                              String sortBy, int page, int limit) {
        Sort sort = switch (sortBy != null ? sortBy.toLowerCase() : "relevance") {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit, sort);

        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate titlePredicate = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate synopsisPredicate = cb.like(cb.lower(root.get("synopsis")), searchPattern);
                predicates.add(cb.or(titlePredicate, synopsisPredicate));
            }
            if (categoryId != null && !categoryId.isBlank() && !"all".equalsIgnoreCase(categoryId)) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (brandId != null && !brandId.isBlank()) {
                predicates.add(cb.equal(root.get("brand").get("id"), brandId));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            if (language != null && !language.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("language")), language.toLowerCase()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> pageResult = productRepository.findAll(spec, pageable);
        List<ProductSummaryDto> items = pageResult.getContent().stream().map(this::mapToSummaryDto).toList();
        return new ProductListResponse(items, pageResult.getTotalElements(), page, limit, pageResult.getTotalPages());
    }

    public ProductDetailDto getProductById(String productId, String postalCode) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));
        return mapToDetailDto(p, postalCode);
    }

    public List<ProductSummaryDto> getRelatedProducts(String productId, int limit) {
        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));
        Pageable pageable = PageRequest.of(0, limit);
        List<Product> related = productRepository.findByCategoryIdAndIdNot(p.getCategory().getId(), productId, pageable);
        return related.stream().map(this::mapToSummaryDto).toList();
    }

    // Mapping helpers
    public CategoryDto mapToCategoryDto(Category c) {
        return new CategoryDto(c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getProductCount());
    }

    public BrandDto mapToBrandDto(Brand b) {
        return new BrandDto(b.getId(), b.getName(), b.getType(), b.getDescription(), b.getAvatarUrl());
    }

    public ProductSummaryDto mapToSummaryDto(Product p) {
        return new ProductSummaryDto(
                p.getId(),
                p.getTitle(),
                mapToBrandDto(p.getBrand()),
                p.getCategory().getName(),
                p.getSynopsis(),
                p.getFormat(),
                p.getPrice(),
                p.getCurrency(),
                p.getCurrencySymbol(),
                p.getTentativeDeliveryDate(),
                p.getCoverImageUrl()
        );
    }

    public ProductDetailDto mapToDetailDto(Product p, String postalCode) {
        String deliveryEst = (postalCode != null && !postalCode.isBlank()) 
                ? "Delivery by Mon, 21 Jul to " + postalCode 
                : p.getTentativeDeliveryDate();

        return new ProductDetailDto(
                p.getId(),
                p.getTitle(),
                mapToBrandDto(p.getBrand()),
                p.getCategory().getName(),
                p.getSynopsis(),
                p.getFullDescription(),
                p.getFormat(),
                p.getPrice(),
                p.getCurrency(),
                p.getCurrencySymbol(),
                deliveryEst,
                p.getTentativeDeliveryDays(),
                p.getCoverImageUrl(),
                p.getIsbn(),
                p.getPublisher(),
                p.getPageCount(),
                p.getLanguage(),
                p.getInStock(),
                p.getStockQuantity()
        );
    }
}
