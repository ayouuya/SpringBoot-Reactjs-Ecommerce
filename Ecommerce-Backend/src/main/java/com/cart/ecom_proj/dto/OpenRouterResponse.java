package com.cart.ecom_proj.dto;

import java.util.List;

public record OpenRouterResponse(List<Choice> choices) {
    public record Choice(OpenRouterMessage message) {}
}
