package com.justeat.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class JustEatApplication {

	public static void main(String[] args) {
		SpringApplication.run(JustEatApplication.class, args);
	}
}
