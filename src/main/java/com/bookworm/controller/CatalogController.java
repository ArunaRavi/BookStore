package com.bookworm.controller;

import com.bookworm.dto.DTOs.*;
import com.bookworm.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@Tag(name = "Catalogue, Categories & Brands", description = "Access categories, authors/brands, products, delivery estimation & related items (Steps 3, 5, 6, 7)")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/categories")
    @Operation(summary = "Access product catalogue categories (Step 3, 5)")
    public ResponseEntity<List<CategoryDto>> getCategories() {
        return ResponseEntity.ok(catalogService.getCategories());
    }

    @GetMapping("/brands")
    @Operation(summary = "Browse brands and writers (Step 6)")
    public ResponseEntity<List<BrandDto>> getBrands(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(catalogService.getBrands(search));
    }

    @GetMapping("/brands/{brandId}/products")
    @Operation(summary = "Get products by brand or writer (Step 6)")
    public ResponseEntity<ProductListResponse> getProductsByBrand(
            @PathVariable String brandId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(catalogService.getProductsByBrand(brandId, page, limit));
    }

    @GetMapping("/products")
    @Operation(summary = "Search and browse products with filters (Step 7)")
    public ResponseEntity<ProductListResponse> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String language,
            @RequestParam(required = false, defaultValue = "relevance") String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(catalogService.searchProducts(search, categoryId, brandId, minPrice, maxPrice, language, sortBy, page, limit));
    }

    @GetMapping("/products/{productId}")
    @Operation(summary = "Get product details with tentative delivery date (Step 7)")
    public ResponseEntity<ProductDetailDto> getProductById(
            @PathVariable String productId,
            @RequestParam(required = false) String postalCode) {
        return ResponseEntity.ok(catalogService.getProductById(productId, postalCode));
    }

    @GetMapping("/products/{productId}/related")
    @Operation(summary = "Get related products (Step 7)")
    public ResponseEntity<List<ProductSummaryDto>> getRelatedProducts(
            @PathVariable String productId,
            @RequestParam(defaultValue = "4") int limit) {
        return ResponseEntity.ok(catalogService.getRelatedProducts(productId, limit));
    }
}
