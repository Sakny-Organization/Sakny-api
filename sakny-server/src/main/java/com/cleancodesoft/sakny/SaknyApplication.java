package com.cleancodesoft.sakny;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = { "com.cleancodesoft.sakny", "com.sakny" })
@EnableJpaRepositories(basePackages = "com.sakny")
@EntityScan(basePackages = "com.sakny")
public class SaknyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaknyApplication.class, args);
	}

}
