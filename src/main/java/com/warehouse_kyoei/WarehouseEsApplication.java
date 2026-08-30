package com.warehouse_kyoei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication()
@EnableJpaAuditing
public class WarehouseEsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WarehouseEsApplication.class, args);
	}

}

