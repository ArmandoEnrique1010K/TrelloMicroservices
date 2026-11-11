package com.trello.identity.security;

import java.time.Duration;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class SecurityConfig {

    private static final String[] NO_AUTH_LIST = {
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-resources/**",
            "/webjars/**",

            "/auth/login",
            "/auth/create-account",
            "/auth/refresh",
            "/token/send/confirm-account",
            "/token/validate/confirm-account",
            "/token/send/password-reset",
            "/token/validate/password-reset",
            "/token/reset-password"
    };

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) -> {

            log.error("EXCEPTION: " + exception);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                        "detail": "Authentication is required to access this resource",
                        "fields": null,
                        "instance": null,
                        "message": "Ha ocurrido un error inesperado",
                        "status": 401,
                        "title": "Unauthorized",
                        "type": "/errors/authentication/not-authenticated"
                    }
                    """);
        };
    }

    // No debe ser un UserDetailsService, sino un AuthenticationManager
    // Restriccion del endpoint para solicitar un JWT interno
    @Bean
    public AuthenticationProvider serviceAuthenticationProvider(
            PasswordEncoder passwordEncoder,
            @Value("${services.clients.project.username}") String username,
            @Value("${services.clients.project.password}") String password) {

        log.info("AUTENTICANDO INTERNAMENTE");

        UserDetails projectService = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .authorities("SERVICE_TOKEN_ISSUE")
                .build();

        // return new InMemoryUserDetailsManager(projectService);
        UserDetailsService serviceUserDetailsService = new InMemoryUserDetailsManager(projectService);

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(serviceUserDetailsService);

        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            AuthenticationProvider serviceAuthenticationProvider)
            throws Exception {

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(NO_AUTH_LIST).permitAll()

                        // Solamente debe permitir el acceso al dominio del microservicio Workflow
                        // http://localhost:8083/business/v1/swagger-ui/index.html

                        // Asi no se define...
                        // .requestMatchers("/auth/internal/service-token")
                        // .permitAll()

                        // Únicamente los servicios autorizados pueden solicitar JWT internos.
                        .requestMatchers("/auth/internal/service-token")
                        .hasAuthority("SERVICE_TOKEN_ISSUE")

                        .anyRequest().authenticated())
                .authenticationProvider(serviceAuthenticationProvider)

                // Habilitar HTTP Basic
                .httpBasic(basic -> basic
                        .authenticationEntryPoint(authenticationEntryPoint()))

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                        // Punto de entrada para excepcion personalizada
                        .authenticationEntryPoint(authenticationEntryPoint()));
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedHeaders(
                Arrays.asList("Origin.Accept", "X-Requested-With", "Content-Type",
                        "Access-Control-Request-Method", "Access-Control-Request-Headers", "Authorization"));
        configuration.setExposedHeaders(
                Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        configuration.setAllowedOrigins(Arrays.asList("/*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS", "PUT", "PATCH"));
        configuration.addAllowedOrigin("*");
        configuration.setMaxAge(Duration.ZERO);
        configuration.setAllowCredentials(Boolean.TRUE);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
