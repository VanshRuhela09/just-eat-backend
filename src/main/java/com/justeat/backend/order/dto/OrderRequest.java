package com.justeat.backend.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderRequest {

    @NotBlank(message = "Address line is mandatory")
    private String addressLine;
}

