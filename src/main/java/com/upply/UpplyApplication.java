package com.upply;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class UpplyApplication {

	public static void main(String[] args) {
		SpringApplication.run(UpplyApplication.class, args);
	}

}
