package com.mystory.twitter.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@ComponentScan("com.mystory.twitter")
@SpringBootApplication
@EnableJpaRepositories("com.mystory.twitter.repository")
@EntityScan("com.mystory.twitter.model")
public class TwitterCollector {
	public static void main(String[] args) {
		SpringApplication.run(TwitterCollector.class, args);
	}
}
