package com.warehouse_kyoei;

import org.springframework.boot.SpringApplication;

public class TestWarehouseEsApplication {

	public static void main(String[] args) {
		SpringApplication.from(WarehouseEsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
