package com.FlashBid_Main.FlashBid_Main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@EnableRedisRepositories
@SpringBootApplication
public class FlashBidMainApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlashBidMainApplication.class, args);
	}

}
