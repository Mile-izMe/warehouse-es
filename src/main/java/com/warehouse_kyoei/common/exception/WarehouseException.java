package com.warehouse_kyoei.common.exception;

import lombok.Getter;

@Getter
public class WarehouseException extends RuntimeException {

    // Whenever business logic is wrong (ex: out of desk, wrong password)
    // Throw this class

    private final ErrorCode errorCode;

    // When there's no need to combine string (default)
    public WarehouseException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    // When want to proceed a new error written manually
    public WarehouseException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }

    // When ErrorCode contains %s (Args)
    public WarehouseException(ErrorCode errorCode, Object... args) {
        super(String.format(errorCode.getDefaultMessage(), args));
        this.errorCode = errorCode;
    }

}