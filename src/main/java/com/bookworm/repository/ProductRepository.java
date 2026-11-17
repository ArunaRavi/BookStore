package com.bookworm.repository;

import com.bookworm.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>, JpaSpecificationExecutor<Product> {
    Page<Product> findByBrandId(String brandId, Pageable pageable);
    List<Product> findByCategoryIdAndIdNot(String categoryId, String productId, Pageable pageable);
    List<Product> findByCategoryIdIn(List<String> categoryIds, Pageable pageable);
}
