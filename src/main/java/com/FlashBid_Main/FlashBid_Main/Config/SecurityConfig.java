package com.FlashBid_Main.FlashBid_Main.Config;

import com.FlashBid_Main.FlashBid_Main.Auth.filter.JwtFilter;
import com.FlashBid_Main.FlashBid_Main.Auth.handler.LoginSuccessHandler;
import com.FlashBid_Main.FlashBid_Main.Auth.handler.LogoutSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;


@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final LoginSuccessHandler loginSuccessHandler;
  private final LogoutSuccessHandler logoutSuccessHandler;
  private final JwtFilter jwtFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())

        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/public/**", "/login", "/signup", "/api/auth/**", "/", "/home").permitAll()
            .anyRequest().authenticated())

        .exceptionHandling(handler -> handler
            .authenticationEntryPoint((request, response, authException) -> {
              String requestedWith = request.getHeader("X-Requested-With");
              String uri = request.getRequestURI();
              if (uri.startsWith("/api/") || "XMLHttpRequest".equals(requestedWith)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
              } else {
                response.sendRedirect("/login");
              }
            })
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

        .formLogin(form -> form
            .loginPage("/login")
            .usernameParameter("userId")
            .successHandler(loginSuccessHandler)
            .permitAll())

        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessHandler(logoutSuccessHandler)
            .permitAll());

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder(){
    return new BCryptPasswordEncoder();
  }
}
