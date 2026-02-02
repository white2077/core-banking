package com.core.bank.demo.contract;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class Request {

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotBlank(message = "Action is required")
    private String action;

    @NotNull(message = "Data is required")
    private Map<String, Object> data;
}
