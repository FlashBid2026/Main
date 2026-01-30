package com.FlashBid_Main.FlashBid_Main;

import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
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
