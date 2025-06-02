package com.example.salon_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example.salon_service")
@EnableScheduling
public class SalonServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(SalonServiceApplication.class, args);
	}
}
