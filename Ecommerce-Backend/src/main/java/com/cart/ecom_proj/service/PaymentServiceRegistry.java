package com.cart.ecom_proj.service;

import com.cart.ecom_proj.model.PaymentMethod;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentServiceRegistry {

    private final Map<PaymentMethod, PaymentService> services = new EnumMap<>(PaymentMethod.class);

    public PaymentServiceRegistry(List<PaymentService> paymentServices) {
        for (PaymentService service : paymentServices) {
            services.put(service.getMethod(), service);
        }
    }

    public PaymentService getService(PaymentMethod method) {
        PaymentService service = services.get(method);
        if (service == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + method);
        }
        return service;
    }
}
