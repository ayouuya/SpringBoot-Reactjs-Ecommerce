package com.cart.ecom_proj.service;

import com.cart.ecom_proj.dto.PaymentRequest;
import com.cart.ecom_proj.dto.PaymentResult;
import com.cart.ecom_proj.model.PaymentMethod;

public interface PaymentService {
    PaymentMethod getMethod();

    PaymentResult process(PaymentRequest request);
}
