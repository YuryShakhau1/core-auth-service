package by.shakhau.ps.auth.config;

import by.shakhau.ps.auth.controller.filter.JwtAuthenticationFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@AllArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.defaultsDisabled().cacheControl(Customizer.withDefaults()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                HttpMethod.GET,
                                "/auth/login",
                                "/auth/public-key",
                                "/auth/users/me").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/roles/users/me").authenticated()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/auth/roles",
                                "/auth/roles/users/**").hasRole("ADMIN")
                        .requestMatchers("/auth/token/**").permitAll()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/users",
                                "/auth/users/create-admin").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/auth/users/change-password").permitAll()
                        .requestMatchers("/auth/users/**", "/auth/users").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
