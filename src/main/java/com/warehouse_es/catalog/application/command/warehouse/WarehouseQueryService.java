package com.warehouse_es.catalog.application.command.warehouse;

import com.warehouse_es.catalog.domain.warehouse.Warehouse;
import com.warehouse_es.catalog.domain.warehouse.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseQueryService {

    private final WarehouseRepository warehouseRepository;

    public Warehouse getById(UUID id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kho!"));
    }

    public List<Warehouse> getAllActiveWarehouses() {
        return warehouseRepository.findAllActive();
    }
}