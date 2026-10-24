package br.com.ptcc.sus.infrastructure.config;

import br.com.ptcc.sus.infrastructure.security.JwtFilter;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtFilter filter) throws Exception {
        return http.csrf(c -> c.disable()).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(a -> a.requestMatchers("/api/auth/**", "/error").permitAll().anyRequest().authenticated()).httpBasic(b -> b.disable()).formLogin(f -> f.disable()).addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class).build();
    }
}