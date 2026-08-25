package com.warehouse_es.catalog.domain.warehouse;

import com.warehouse_es.common.exception.ErrorCode;
import com.warehouse_es.common.exception.WarehouseException;
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

    // BEHAVIORS: BUSINESS LOGIC

    /**
     * Factory method to create a new Warehouse from user's Request.
     * Service call this func instead of "new" hay Builder outside.
     */
    public static Warehouse createNew(String warehouseCode, String name, String address) {
        if (warehouseCode == null || warehouseCode.trim().isEmpty()) {
            throw new WarehouseException(ErrorCode.WAREHOUSE_CODE_NULL);
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
            throw new WarehouseException(ErrorCode.WAREHOUSE_NOT_ABLE_TO_UPDATE);
        }

        this.name = newName;
        this.address = newAddress;
    }

    /**
     * Stop working
     */
    public void deactivate() {
        if (this.status == WarehouseStatus.INACTIVE) {
            throw new WarehouseException(ErrorCode.WAREHOUSE_INACTIVE);
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