package com.FlashBid_Main.FlashBid_Main.Config;

import com.maxmind.geoip2.DatabaseReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class GeoIpConfig {

    @Value("${geoip.database-path}")
    private Resource databaseResource;

    @Bean
    public DatabaseReader databaseReader() throws IOException {
        InputStream inputStream = databaseResource.getInputStream();
        return new DatabaseReader.Builder(inputStream).build();
    }
}
