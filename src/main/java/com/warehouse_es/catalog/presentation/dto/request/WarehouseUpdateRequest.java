package com.warehouse_es.catalog.presentation.dto.request;

import java.util.UUID;

public record WarehouseUpdateRequest (
    String name,
    String address
) {}