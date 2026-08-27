package com.warehouse_kyoei.common.exception;

public record FieldErrorDetail(
        String field,
        String message
) {}