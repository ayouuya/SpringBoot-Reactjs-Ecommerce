package com.cart.ecom_proj.service;

import com.cart.ecom_proj.dto.PayPalPaymentDetails;
import com.cart.ecom_proj.dto.PaymentRequest;
import com.cart.ecom_proj.dto.PaymentResult;
import com.cart.ecom_proj.model.PaymentMethod;
import com.cart.ecom_proj.model.PaymentStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class PayPalPaymentService implements PaymentService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.PAYPAL;
    }

    @Override
    public PaymentResult process(PaymentRequest request) {
        PayPalPaymentDetails paypal = request.paypal();
        if (paypal == null || paypal.email() == null || !EMAIL_PATTERN.matcher(paypal.email()).matches()) {
            return new PaymentResult(PaymentStatus.DECLINED, null, "Valid PayPal email is required.");
        }

        String reference = "PAYPAL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResult(PaymentStatus.APPROVED, reference, "PayPal payment approved.");
    }
}
