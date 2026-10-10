package com.springboot.AutomobileInsurance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)

public class AutomobileInsuranceSystemApplication {

	public static void main(String[] args) {

		SpringApplication.run(AutomobileInsuranceSystemApplication.class, args);
	}

}
