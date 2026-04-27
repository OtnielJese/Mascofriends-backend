package com.vetivet.config;

import com.vetivet.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC ENDPOINTS (no authentication required)
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/public/**").permitAll()
                        
                        // HEALTH CHECK ENDPOINTS - Public for monitoring
                        .requestMatchers("/health/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        
                        // CORS preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // ACTUATOR SECURITY - All other actuator endpoints require ADMIN
                        // This blocks: /actuator/env, /actuator/beans, /actuator/metrics, etc.
                        .requestMatchers("/actuator/**").hasAnyAuthority("ADMIN", "SUPER_ADMIN")
                        
                        // ADMIN-ONLY CRUD OPERATIONS
                        .requestMatchers("/owners/**").hasAnyAuthority("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/patients/**").hasAnyAuthority("ADMIN", "SUPER_ADMIN")
                        
                        // APPOINTMENTS - Public create, Admin-only read/update/delete
                        .requestMatchers(HttpMethod.POST, "/appointments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/appointments/**").hasAnyAuthority("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/appointments/**").hasAnyAuthority("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/appointments/**").hasAnyAuthority("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/appointments/**").hasAnyAuthority("ADMIN", "SUPER_ADMIN")
                        
                        // WHATSAPP INTEGRATION - Public
                        .requestMatchers("/whatsapp/**").permitAll()
                        
                        // DEFAULT - Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers
                        .frameOptions(fo -> fo.sameOrigin())
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:"))
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
