package com.ecommerce.payment.model;

import com.ecommerce.payment.payload.request.PaymentMethod;
import com.ecommerce.payment.payload.request.PaymentStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "payment")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Payment {
  @Id private String paymentId;
  private String orderId;

  @Enumerated(EnumType.STRING)
  private PaymentStatus paymentStatus;

  @Enumerated(EnumType.STRING)
  private PaymentMethod paymentMethod;

  private Long totalAmount;
}
