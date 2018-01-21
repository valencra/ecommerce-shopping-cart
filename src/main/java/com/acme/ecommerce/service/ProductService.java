package com.acme.ecommerce.service;

import com.acme.ecommerce.domain.Product;
import com.acme.ecommerce.exception.ProductOrderQuantityExceedsAvailabilityException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

  Iterable<Product> findAll();

  Page<Product> findAll(Pageable pageable);

  Product findById(Long id);

  void checkQuantity(Product orderProduct, Integer orderQuantity) throws
      ProductOrderQuantityExceedsAvailabilityException;
}
