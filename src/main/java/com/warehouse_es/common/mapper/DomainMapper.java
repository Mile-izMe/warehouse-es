package com.warehouse_es.common.mapper;

import java.util.List;

/**
 * @param <D> Domain Model (E.g: Warehouse)
 * @param <E> JPA Entity (E.g: WarehouseEntity)
 */
public interface DomainMapper<D, E> {

    D toDomain(E entity);

    E toEntity(D domain);

    List<D> toDomainList(List<E> entities);

    List<E> toEntityList(List<D> domains);
}