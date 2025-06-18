package com.ecommerce.payment.payload.response;

import java.io.Serializable;

public class PaymentResponse implements Serializable {

  private String status;
  private String paymentLinkUrl;
  private String paymentLinkId;

  public PaymentResponse() {}

  public PaymentResponse(String status, String paymentLinkId, String paymentLinkUrl) {

    this.status = status;
    this.paymentLinkId = paymentLinkId;
    this.paymentLinkUrl = paymentLinkUrl;
  }

  public String getStatus() {

    return status;
  }

  public void setStatus(String status) {

    this.status = status;
  }

  public String getPaymentLinkId() {

    return paymentLinkId;
  }

  public void setPaymentLinkId(String paymentLinkId) {

    this.paymentLinkId = paymentLinkId;
  }

  public String getPaymentLinkUrl() {

    return paymentLinkUrl;
  }

  public void setPaymentLinkUrl(String paymentLinkUrl) {

    this.paymentLinkUrl = paymentLinkUrl;
  }

  @Override
  public String toString() {

    return "PaymentResponse{"
        + "status='"
        + status
        + '\''
        + ", paymentId='"
        + paymentLinkId
        + '\''
        + ", paymentUrl='"
        + paymentLinkUrl
        + '\''
        + '}';
  }
}
