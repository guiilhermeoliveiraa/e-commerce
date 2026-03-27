package com.javacore.spring_api_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SpringApiAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringApiAppApplication.class, args);
	}

}
