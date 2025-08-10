package com.example.book.service.repo;

import com.example.book.service.model.Order;
import com.example.book.service.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByClientIdAndStatus(Long clientId, OrderStatus status);

    @Query("SELECT o FROM Order o JOIN o.client c WHERE LOWER(c.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<Order> findOrdersByClientEmail(@Param("email") String email);

    @Query("SELECT o FROM Order o WHERE o.client.email = :clientEmail AND o.status = :status")
    List<Order> findAllByClientEmailAndStatus(@Param("clientEmail") String clientEmail, @Param("status") OrderStatus status);


    @Query("SELECT o FROM Order o WHERE o.client.email = :clientEmail AND o.status <> :status")
    List<Order> findAllByClientEmailAndStatusNot(@Param("clientEmail") String clientEmail, @Param("status") OrderStatus status);
}
