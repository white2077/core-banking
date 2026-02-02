package com.core.bank.demo.util;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import com.core.bank.demo.config.exception.BusinessException;
import com.core.bank.demo.config.exception.ErrorCode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class DataMapperUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final Validator VALIDATOR;

    static {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            VALIDATOR = factory.getValidator();
        }
    }

    private DataMapperUtil() {
    }

    public static <T> T toObject(Object data, Class<T> clazz) {
        return MAPPER.convertValue(data, clazz);
    }

    public static <T> T toObjectWithValidation(Object data, Class<T> clazz) {
        T result = MAPPER.convertValue(data, clazz);
        validate(result);
        return result;
    }

    public static <T> void validate(T object) {
        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(object);
        if (!violations.isEmpty()) {
            Map<String, String> errors = violations.stream().collect(Collectors.toMap(
                    v -> v.getPropertyPath().toString(), ConstraintViolation::getMessage, (v1, v2) -> v1 + "; " + v2));
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Validation failed", errors);
        }
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
