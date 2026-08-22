package com.warehouse_es.catalog.presentation.warehouse;

import com.warehouse_es.catalog.application.command.warehouse.WarehouseCommand.CreateWarehouseCommand;
import com.warehouse_es.catalog.application.command.warehouse.WarehouseCommand.UpdateWarehouseCommand;
import com.warehouse_es.catalog.application.command.warehouse.WarehouseCommandService;
import com.warehouse_es.catalog.application.command.warehouse.WarehouseQueryService;
import com.warehouse_es.catalog.domain.warehouse.Warehouse;
import com.warehouse_es.catalog.presentation.dto.request.WarehouseCreateRequest;
import com.warehouse_es.catalog.presentation.dto.request.WarehouseUpdateRequest;
import com.warehouse_es.catalog.presentation.dto.response.WarehouseResponse;
import com.warehouse_es.catalog.presentation.mapper.WarehouseWebMapper;
import com.warehouse_es.common.response.ApiSuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseCommandService commandService;
    private final WarehouseQueryService queryService;
    private final WarehouseWebMapper webMapper;

    // --- API QUERY ---
    @GetMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<WarehouseResponse>> getById(@PathVariable UUID id) {
        Warehouse warehouse = queryService.getById(id);
        WarehouseResponse responseData = webMapper.toResponse(warehouse);

        return ResponseEntity.ok(ApiSuccessResponse.<WarehouseResponse>builder()
                .message("Get warehouse information success!")
                .data(responseData)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<List<WarehouseResponse>>> getAll() {
        List<Warehouse> warehouses = queryService.getAllActiveWarehouses();
        List<WarehouseResponse> responses = warehouses.stream()
                .map(webMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiSuccessResponse.<List<WarehouseResponse>>builder()
                .message("Get list warehouses success!")
                .data(responses)
                .build());
    }

    // --- API COMMAND ---
    @PostMapping
    public ResponseEntity<ApiSuccessResponse<WarehouseResponse>> create(
            @Valid @RequestBody WarehouseCreateRequest request
    ) {
        // Map HTTP Request -> Command Object
        CreateWarehouseCommand command = CreateWarehouseCommand.builder()
                .code(request.warehouseCode())
                .name(request.name())
                .address(request.address())
                .build();

        // Handle Command
        Warehouse createdWarehouse = commandService.createWarehouse(command);
        WarehouseResponse responseData = webMapper.toResponse(createdWarehouse);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiSuccessResponse.<WarehouseResponse>builder()
                        .message("Tạo mới kho thành công!")
                        .data(responseData)
                        .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<WarehouseResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody WarehouseUpdateRequest request
    ) {
        // Map HTTP Request -> Command Object
        UpdateWarehouseCommand command = UpdateWarehouseCommand.builder()
                .id(id)
                .name(request.name())
                .address(request.address())
                .build();

        // Handle Command
        Warehouse updatedWarehouse = commandService.updateWarehouse(command);
        WarehouseResponse responseData = webMapper.toResponse(updatedWarehouse);

        ApiSuccessResponse<WarehouseResponse> apiResponse = ApiSuccessResponse.<WarehouseResponse>builder()
                .message("Update warehouse information success!")
                .data(responseData)
                .build();

        // Return Response
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        commandService.deactivateWarehouse(id);
        return ResponseEntity.noContent().build();
    }
}
