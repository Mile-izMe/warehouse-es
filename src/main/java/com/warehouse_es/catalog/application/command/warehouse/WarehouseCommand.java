package com.warehouse_es.catalog.application.command.warehouse;

import lombok.Builder;

import java.util.UUID;

public class WarehouseCommand {

    @Builder
    public record CreateWarehouseCommand(String code, String name, String address) {}

    @Builder
    public record UpdateWarehouseCommand(UUID id, String name, String address) {}

}
