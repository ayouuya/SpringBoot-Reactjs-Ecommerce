package com.cart.ecom_proj.dto;

import java.util.List;

public record OpenRouterRequest(String model, List<OpenRouterMessage> messages) {}
