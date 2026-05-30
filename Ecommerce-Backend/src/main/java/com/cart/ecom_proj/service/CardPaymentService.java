package com.cart.ecom_proj.service;

import com.cart.ecom_proj.dto.CardPaymentDetails;
import com.cart.ecom_proj.dto.PaymentRequest;
import com.cart.ecom_proj.dto.PaymentResult;
import com.cart.ecom_proj.model.PaymentMethod;
import com.cart.ecom_proj.model.PaymentStatus;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.UUID;

@Service
public class CardPaymentService implements PaymentService {

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.CARD;
    }

    @Override
    public PaymentResult process(PaymentRequest request) {
        CardPaymentDetails card = request.card();
        if (card == null) {
            return new PaymentResult(PaymentStatus.DECLINED, null, "Card details are required.");
        }

        String number = normalizeCardNumber(card.cardNumber());
        if (number.isEmpty() || number.length() < 13 || number.length() > 19) {
            return new PaymentResult(PaymentStatus.DECLINED, null, "Invalid card number.");
        }

        if (!isLuhnValid(number)) {
            return new PaymentResult(PaymentStatus.DECLINED, null, "Card number failed validation.");
        }

        if (!isValidExpiry(card.expiryMonth(), card.expiryYear())) {
            return new PaymentResult(PaymentStatus.DECLINED, null, "Card is expired.");
        }

        if (!isValidCvv(card.cvv())) {
            return new PaymentResult(PaymentStatus.DECLINED, null, "Invalid CVV.");
        }

        String reference = "CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new PaymentResult(PaymentStatus.APPROVED, reference, "Card payment approved.");
    }

    private String normalizeCardNumber(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replaceAll("[^0-9]", "");
    }

    private boolean isValidCvv(String cvv) {
        if (cvv == null) {
            return false;
        }
        String trimmed = cvv.trim();
        return trimmed.length() == 3 || trimmed.length() == 4;
    }

    private boolean isValidExpiry(String monthValue, String yearValue) {
        if (monthValue == null || yearValue == null) {
            return false;
        }

        try {
            int month = Integer.parseInt(monthValue.trim());
            int year = Integer.parseInt(yearValue.trim());
            if (year < 100) {
                year += 2000;
            }
            if (month < 1 || month > 12) {
                return false;
            }
            YearMonth expiry = YearMonth.of(year, month);
            YearMonth now = YearMonth.now();
            return !expiry.isBefore(now);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isLuhnValid(String number) {
        int sum = 0;
        boolean alternate = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = number.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }
}
