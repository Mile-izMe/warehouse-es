package com.warehouse_kyoei.catalog.application.command.warehouse;

import com.warehouse_kyoei.catalog.domain.warehouse.Warehouse;
import com.warehouse_kyoei.catalog.domain.warehouse.WarehouseRepository;
import com.warehouse_kyoei.catalog.application.command.warehouse.WarehouseCommand.CreateWarehouseCommand;
import com.warehouse_kyoei.catalog.application.command.warehouse.WarehouseCommand.UpdateWarehouseCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseCommandService {

    private final WarehouseRepository warehouseRepository;

    // Command Object (Receive data from Controller, # HTTP Request)
    @Transactional
    public Warehouse createWarehouse(CreateWarehouseCommand cmd) {
        if (warehouseRepository.existsByWarehouseCodeAndStatus(cmd.code(), "ACTIVE")) {
            throw new IllegalArgumentException("Mã kho đã tồn tại và đang hoạt động!");
        }

        // Gọi Factory Method của Domain
        Warehouse newWarehouse = Warehouse.createNew(cmd.code(), cmd.name(), cmd.address());

        return warehouseRepository.save(newWarehouse);
    }

    @Transactional
    public Warehouse updateWarehouse(UpdateWarehouseCommand cmd) {
        Warehouse warehouse = warehouseRepository.findById(cmd.id())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kho!"));

        warehouse.updateInfo(cmd.name(), cmd.address());

        return warehouseRepository.save(warehouse);
    }

    @Transactional
    public void deactivateWarehouse(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kho!"));

        warehouse.deactivate();
        warehouseRepository.save(warehouse);
    }
}
