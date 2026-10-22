package com.BEPerfectSquat.BEPerfectSquat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BePerfectSquatApplication {

	public static void main(String[] args) {
		SpringApplication.run(BePerfectSquatApplication.class, args);
	}

}
