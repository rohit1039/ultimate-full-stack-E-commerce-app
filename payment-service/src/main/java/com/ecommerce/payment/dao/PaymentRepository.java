package com.ecommerce.payment.dao;

import com.ecommerce.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, String> {

  Payment findByOrderId(String orderId);
}
