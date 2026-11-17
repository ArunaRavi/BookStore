package com.bookworm.repository;

import com.bookworm.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Page<Order> findByUserIdOrderByOrderTimestampDesc(String userId, Pageable pageable);
    List<Order> findByUserIdOrderByOrderTimestampDesc(String userId);
    Optional<Order> findByIdAndUserId(String id, String userId);
}
