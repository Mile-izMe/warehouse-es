package com.warehouse_kyoei.catalog.infrastructure.warehouse;

import com.warehouse_kyoei.catalog.domain.warehouse.Warehouse;
import com.warehouse_kyoei.catalog.domain.warehouse.WarehouseRepository;
import com.warehouse_kyoei.catalog.domain.warehouse.WarehouseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WarehouseRepositoryImpl implements WarehouseRepository {

    // Inject Spring Data JPA
    private final SpringDataWarehouseRepository jpaRepository;

    // Inject Mapper to convert between Entity & Domain Model
    private final WarehouseMapper mapper;

    @Override
    public Optional<Warehouse> findByWarehouseCode(String warehouseCode) {
        return jpaRepository.findByWarehouseCode(warehouseCode)
                .map(mapper::toDomain); // Get from DB Entity -> Map to Domain Model
    }

    @Override
    public boolean existsByWarehouseCodeAndStatus(String warehouseCode, WarehouseStatus status) {
        return jpaRepository.existsByWarehouseCodeAndStatus(warehouseCode, status);
    }

    @Override
    public Warehouse save(Warehouse warehouse) {
        // Map from Domain Model -> Entity to save DB
        WarehouseEntity entity = mapper.toEntity(warehouse);
        WarehouseEntity savedEntity = jpaRepository.save(entity);

        // Return Domain Model
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Warehouse> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Warehouse> findAllActive() {
        List<WarehouseEntity> activeEntities = jpaRepository.findAllByStatus(WarehouseStatus.ACTIVE);

        return mapper.toDomainList(activeEntities);
    }

    @Override
    public int findNumDataInDatabase() {
        return Math.toIntExact(jpaRepository.count());
    }

    @Override
    public List<Warehouse> saveAll(List<Warehouse> warehouses) {
        // Turn List<Domain> -> List<Entity>
        List<WarehouseEntity> entities = mapper.toEntityList(warehouses);

        List<WarehouseEntity> savedEntities = jpaRepository.saveAll(entities);

        // Turn List<Entity> -> List<Domain>
        return mapper.toDomainList(savedEntities);
    }
}