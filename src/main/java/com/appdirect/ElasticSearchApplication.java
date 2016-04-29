package com.appdirect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class ElasticSearchApplication {
	public static void main(String[] args) {
		SpringApplication.run(ElasticSearchApplication.class, args);
	}
}
