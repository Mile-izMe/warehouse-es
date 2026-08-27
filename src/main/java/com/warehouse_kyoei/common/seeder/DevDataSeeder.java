package com.warehouse_kyoei.common.seeder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse_kyoei.catalog.domain.product.Product;
import com.warehouse_kyoei.catalog.domain.product.ProductRepository;
import com.warehouse_kyoei.catalog.domain.warehouse.Warehouse;
import com.warehouse_kyoei.catalog.domain.warehouse.WarehouseRepository;
import com.warehouse_kyoei.shared.relay.EventPublishCursor;
import com.warehouse_kyoei.shared.relay.EventPublishCursorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final EventPublishCursorRepository cursorRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) {
        log.info("Start checking and prepare environment for DEV...");
        seedWarehouses();
        seedProducts();
        log.info("Finish seeding data!");
    }

    private void seedWarehouses() {
        if (warehouseRepository.findNumDataInDatabase() == 0) {
            log.info("Seeding data for Warehouse...");

            Warehouse wh1 = Warehouse.createNew(
                    "WH-HCM-01",
                    "Kho Tổng Miền Nam",
                    "Quận 9, TP.HCM"
            );

            Warehouse wh2 = Warehouse.createNew(
                    "WH-HN-01",
                    "Kho Trạm Cầu Giấy",
                    "Cầu Giấy, Hà Nội"
            );

            warehouseRepository.saveAll(List.of(wh1, wh2));
            log.info("Seeded success {} storage.", 2);
        } else {
            log.info("Warehouse'data existed, passing seed.");
        }
    }

    private void seedProducts() {
        if (productRepository.count() == 0) {
            log.info("Seeding Product data...");

            try (InputStream inputStream = getClass().getResourceAsStream("/seed/products.json")) {

                // 1. Convert JSON to List<Product> using Jackson
                List<Product> products = objectMapper.readValue(inputStream, new TypeReference<List<Product>>() {});

                // 2. Save all to DB
                productRepository.saveAll(products);

                log.info("Seeded success {} product from file JSON.", products.size());

            } catch (Exception e) {
                // NOTE: Log the error if the file is missing or JSON is malformed
                log.error("Error seeding Product from JSON: {}", e.getMessage(), e);
            }
        } else {
            log.info("Product'data existed, passing seed.");
        }
    }

    private void seedKafkaCursor() {
        boolean result = cursorRepository.existsByWorkerId("kafka_main_relay");
        if (!result) {
            log.info("Seeding data for Cursor...");

            EventPublishCursor cursor = EventPublishCursor.builder()
                    .workerId("kafka_main_relay")
                    .lastProcessedEventId(0)
                    .build();

            cursorRepository.save(cursor);
            log.info("Seeded success {} cursor.", 1);
        } else {
            log.info("Cursor'data existed, passing seed.");
        }
    }
}
