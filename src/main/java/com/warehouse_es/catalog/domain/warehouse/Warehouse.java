package com.warehouse_es.catalog.domain.warehouse;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class Warehouse {

    private UUID id;
    private String warehouseCode;
    private String name;
    private String address;
    private WarehouseStatus status;

    // 2. BEHAVIORS: BUSINESS LOGIC

    /**
     * Factory method to create a new Warehouse from user's Request.
     * Service call this func instead of "new" hay Builder outside.
     */
    public static Warehouse createNew(String warehouseCode, String name, String address) {
        if (warehouseCode == null || warehouseCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã kho không được để trống!");
        }

        return Warehouse.builder()
                .warehouseCode(warehouseCode)
                .name(name)
                .address(address)
                .status(WarehouseStatus.ACTIVE)
                .build();
    }

    /**
     * Update warehouse in4
     */
    public void updateInfo(String newName, String newAddress) {
        // Business rule: Warehouse inactive not allowed to update
        if (this.status == WarehouseStatus.INACTIVE) {
            throw new IllegalStateException("Không thể sửa thông tin khi kho đang bị khóa (INACTIVE)!");
        }

        this.name = newName;
        this.address = newAddress;
    }

    /**
     * Stop working
     */
    public void deactivate() {
        if (this.status == WarehouseStatus.INACTIVE) {
            throw new IllegalStateException("Kho này đã bị khóa từ trước!");
        }
        this.status = WarehouseStatus.INACTIVE;
    }

    /**
     * Reopen warehouse
     */
    public void activate() {
        this.status = WarehouseStatus.ACTIVE;
    }
}