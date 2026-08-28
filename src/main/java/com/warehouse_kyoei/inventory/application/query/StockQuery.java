package com.warehouse_kyoei.inventory.application.query;

import lombok.Builder;

import java.time.LocalDate;

public class StockQuery {

    @Builder
    public record GetStockQuery(String warehouseCode, String skuCode) {}

    @Builder
    public record GetStockDailyQuery(String warehouseCode, String skuCode,
                                     LocalDate from, LocalDate to) {}

}
