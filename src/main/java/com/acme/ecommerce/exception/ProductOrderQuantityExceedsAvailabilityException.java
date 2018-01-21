package com.acme.ecommerce.exception;

import com.acme.ecommerce.domain.Product;

public class ProductOrderQuantityExceedsAvailabilityException extends RuntimeException {
  public ProductOrderQuantityExceedsAvailabilityException(Product orderProduct, Integer orderQuantity) {
    super(
        String.format(
            "Unable to make %d order/s of %s: only %d available",
            orderQuantity, orderProduct.getName(), orderProduct.getQuantity()
        )
    );
  }
}
