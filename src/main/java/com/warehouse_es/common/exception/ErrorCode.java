package com.warehouse_es.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // --- Common Error System ---
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS-500", "System error not defined!"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "SYS-400", "Input not valid!"),
    CONFLICT_ERROR(HttpStatus.CONFLICT, "SYS-409", "Data has conflicts!"),

    // --- Business Error ---
    // --- Stock errors ---
    PRODUCT_WAREHOUSE_INVALID(HttpStatus.BAD_REQUEST, "SKU-001", "Warehouse or SKU is invalid/inactive!"),
    IMPORT_QUANTITY_INVALID(HttpStatus.BAD_REQUEST, "SKU-002", "Import quantity must larger than 0!"),
    EXPORT_QUANTITY_INVALID(HttpStatus.BAD_REQUEST, "SKU-003", "Phone already existed in system"),
    ADJUSTMENT_INVALID(HttpStatus.BAD_REQUEST, "SKU-004", "Token is invalid"),
    UNSUPPORTED_EVENT(HttpStatus.INTERNAL_SERVER_ERROR, "SKU-005", "Event not defined in apply(): %s");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String code, String defaultMessage) {
        this.status = status;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
