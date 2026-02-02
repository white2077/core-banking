package com.core.bank.demo.config.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Success
    SUCCESS("00", "Success"),

    // System Errors (SYS_XXX)
    SYSTEM_ERROR("SYS_001", "Internal system error"), SERVICE_UNAVAILABLE("SYS_002",
            "Service temporarily unavailable"), TIMEOUT_ERROR("SYS_003", "Request timeout"),

    // Validation Errors (VAL_XXX)
    VALIDATION_ERROR("VAL_001", "Validation failed"), INVALID_REQUEST("VAL_002",
            "Invalid request format"), MISSING_REQUIRED_FIELD("VAL_003",
                    "Missing required field"), INVALID_FIELD_FORMAT("VAL_004", "Invalid field format"),

    // Authentication Errors (AUTH_XXX)
    AUTHENTICATION_FAILED("AUTH_001", "Authentication failed"), INVALID_TOKEN("AUTH_002",
            "Invalid or expired token"), ACCESS_DENIED("AUTH_003",
                    "Access denied"), MISSING_AUTHORIZATION("AUTH_004", "Missing authorization header"),

    // Business Errors - General (BIZ_XXX)
    BUSINESS_ERROR("BIZ_001", "Business rule violation"), RESOURCE_NOT_FOUND("BIZ_002",
            "Resource not found"), DUPLICATE_RESOURCE("BIZ_003",
                    "Resource already exists"), INVALID_STATE("BIZ_004", "Invalid state transition"),

    // Business Errors - Customer (CUST_XXX)
    CUSTOMER_NOT_FOUND("CUST_001", "Customer not found"), CUSTOMER_ALREADY_EXISTS("CUST_002",
            "Customer already exists"), INVALID_CUSTOMER_STATUS("CUST_003", "Invalid customer status"),

    // Operation Errors (OP_XXX)
    UNSUPPORTED_OPERATION("OP_001", "Unsupported operation"), OPERATION_FAILED("OP_002", "Operation execution failed"),

    // Header Errors (HDR_XXX)
    MISSING_HEADER("HDR_001", "Missing required header");

    private final String code;
    private final String message;
}
