package com.warehouse_es.common.exception;

public record FieldErrorDetail(
        String field,
        String message
) {}