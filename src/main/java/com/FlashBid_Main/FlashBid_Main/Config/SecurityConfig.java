package com.FlashBid_Main.FlashBid_Main.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@EnableWebSecurity
@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http){
    http
        .csrf(csrf -> csrf.disable())

        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/public/**", "/login").permitAll()
            .anyRequest().authenticated())

        .formLogin(form -> form
            .loginPage("/login")
            .defaultSuccessUrl("/home")
            .permitAll())

        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
            .permitAll());

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }
}
