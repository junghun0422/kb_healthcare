package com.kb.healthcare.record;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.kb.healthcare")
@EntityScan(basePackages = "com.kb.healthcare")
@ComponentScan(basePackages = {
	"com.kb.healthcare.authserver",
	"com.kb.healthcare.common",
	"com.kb.healthcare.record"
})
@SpringBootApplication
public class RecordApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecordApplication.class, args);
	}

}
