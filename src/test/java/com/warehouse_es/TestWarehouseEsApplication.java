package com.warehouse_es;

import org.springframework.boot.SpringApplication;

public class TestWarehouseEsApplication {

	public static void main(String[] args) {
		SpringApplication.from(WarehouseEsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
