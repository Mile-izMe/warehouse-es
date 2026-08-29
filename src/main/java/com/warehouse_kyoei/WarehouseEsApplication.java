package com.warehouse_kyoei;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication()
@EnableJpaAuditing
@ComponentScan(basePackages = {"com.warehouse_kyoei"})
public class WarehouseEsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WarehouseEsApplication.class, args);
	}

}

@RestController
class TestController {
	@GetMapping("/api/ping")
	public String ping() {
		return "pong";
	}
}
