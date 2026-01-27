package com.FlashBid_Main.FlashBid_Main.Auth.util;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationExtractor {

    private final DatabaseReader databaseReader;

    public String extractIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    public String extractCountry(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            CityResponse response = databaseReader.city(inetAddress);

            String country = response.getCountry().getName();
            return country != null ? country : "Unknown";

        } catch (Exception e) {
            log.warn("Failed to extract country from IP: {}", ipAddress, e);
            return "Unknown";
        }
    }

    public String extractCity(String ipAddress) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipAddress);
            CityResponse response = databaseReader.city(inetAddress);

            String city = response.getCity().getName();
            return city != null ? city : "Unknown";

        } catch (Exception e) {
            log.warn("Failed to extract city from IP: {}", ipAddress, e);
            return "Unknown";
        }
    }

    public LocationInfo extractFullLocation(HttpServletRequest request) {
        String ip = extractIpAddress(request);

        if (isLocalOrPrivateIp(ip)) {
            return new LocationInfo(ip, "Local", "Local");
        }

        String country = extractCountry(ip);
        String city = extractCity(ip);

        return new LocationInfo(ip, country, city);
    }

    private boolean isLocalOrPrivateIp(String ip) {
        return ip.equals("127.0.0.1") ||
               ip.equals("0:0:0:0:0:0:0:1") ||
               ip.startsWith("192.168.") ||
               ip.startsWith("10.") ||
               ip.startsWith("172.");
    }

    public record LocationInfo(String ipAddress, String country, String city) {}
}
