package com.trello.identity.security;

import java.time.Duration;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
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
import org.springframework.security.web.access.AccessDeniedHandler;
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

    // Endpoints públicos que no requieren autenticación.
    // Se incluyen los recursos de Swagger y los endpoints necesarios
    // para el registro, autenticación y recuperación de cuentas.
    private static final String[] PUBLIC_ENDPOINTS = {
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

    // Maneja los errores de autenticación (HTTP 401 Unauthorized).
    // Se ejecuta cuando el cliente intenta acceder a un recurso protegido
    // sin presentar credenciales válidas o sin estar autenticado.
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, exception) -> {

            // log.error("EXCEPTION: " + exception);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
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

    // Maneja los errores de autorización (HTTP 403 Forbidden).
    // Se ejecuta cuando el cliente está autenticado correctamente,
    // pero no dispone de las autoridades o permisos necesarios
    // para acceder al recurso solicitado.
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> {

            // log.error("ACCESS DENIED: ", exception);

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                    {
                        "detail": "You do not have the required permissions to access this resource",
                        "fields": null,
                        "instance": null,
                        "message": "No tienes permisos para acceder a este recurso",
                        "status": 403,
                        "title": "Forbidden",
                        "type": "/errors/authorization/forbidden"
                    }
                    """);
        };
    }

    // Proveedor de autenticación para los servicios internos autorizados.
    // Utiliza autenticación HTTP Basic con credenciales almacenadas
    // en las propiedades de configuración del microservicio.
    //
    // Su finalidad es restringir la generación de JWT internos,
    // evitando que cualquier cliente pueda obtener un token de servicio.
    @Bean
    public AuthenticationProvider serviceAuthenticationProvider(
            PasswordEncoder passwordEncoder,
            // Se le pasa el username y el password tal y como esta definido en
            // applications.properties
            @Value("${services.clients.project.username}") String username,
            @Value("${services.clients.project.password}") String password) {

        // Se construye un usuario interno en memoria con una autoridad
        // exclusiva para solicitar tokens de servicio.
        UserDetails projectService = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .authorities("SERVICE_TOKEN_ISSUE")
                .build();

        // Almacena las credenciales del servicio en memoria.
        UserDetailsService serviceUserDetailsService = new InMemoryUserDetailsManager(projectService);

        // Proveedor encargado de validar las credenciales recibidas
        // mediante HTTP Basic.
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(serviceUserDetailsService);

        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            AuthenticationProvider serviceAuthenticationProvider)
            throws Exception {

        http
                // Se deshabilita CSRF porque la aplicación utiliza autenticación
                // basada en tokens y no mantiene sesiones HTTP tradicionales.
                .csrf(csrf -> csrf.disable())

                // Configuración sin estado: cada petición debe proporcionar
                // sus propias credenciales de autenticación.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Definición de las reglas de autorización de los endpoints.
                .authorizeHttpRequests(auth -> auth

                        // Los endpoints públicos no requieren autenticación.
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                        // Solamente debe permitir el acceso al dominio del microservicio Workflow
                        // http://localhost:8083/business/v1/swagger-ui/index.html

                        // Asi no se define...
                        // .requestMatchers("/auth/internal/service-token")
                        // .permitAll()

                        // Endpoint exclusivo para solicitar JWT internos.
                        // Solamente los servicios autenticados mediante HTTP Basic
                        // y con la autoridad SERVICE_TOKEN_ISSUE pueden acceder.
                        .requestMatchers("/auth/internal/service-token")
                        .hasAuthority("SERVICE_TOKEN_ISSUE")

                        // Todos los demás endpoints requieren un JWT de usuario
                        // cuyo scope debe contener el valor "user".
                        //
                        // Spring Security convierte automáticamente el scope
                        // del JWT en una autoridad con el prefijo SCOPE_.
                        .anyRequest().hasAuthority(
                                "SCOPE_user")
                // En entornos de pruebas se podría utilizar:
                // .anyRequest().authenticated()
                //
                // Esto permitiría el acceso a cualquier usuario autenticado,
                // independientemente de sus autoridades.
                )

                // Registra el proveedor encargado de autenticar
                // las credenciales HTTP Basic de los servicios internos.
                .authenticationProvider(serviceAuthenticationProvider)

                // Configura los manejadores globales de excepciones de seguridad.
                .exceptionHandling(exception -> exception
                        // HTTP 401: el cliente no está autenticado correctamente.
                        .authenticationEntryPoint(authenticationEntryPoint())

                        // HTTP 403: el cliente está autenticado, pero no autorizado.
                        .accessDeniedHandler(
                                accessDeniedHandler()))
                // Habilita HTTP Basic para autenticar servicios internos.
                .httpBasic(basic -> basic
                        .authenticationEntryPoint(authenticationEntryPoint()))

                // Configura este microservicio como OAuth2 Resource Server.
                // Permite recibir y validar JWT en las peticiones HTTP.
                .oauth2ResourceServer(oauth2 -> oauth2
                        // Utiliza el JwtDecoder configurado para validar
                        // la firma, expiración y demás propiedades del JWT.
                        .jwt(Customizer.withDefaults())
                        // Maneja errores de autenticación durante la validación
                        // y procesamiento del JWT.
                        .authenticationEntryPoint(authenticationEntryPoint())

                        // Maneja errores de autorización relacionados con JWT,
                        // por ejemplo, cuando el token no tiene el scope requerido.
                        .accessDeniedHandler(accessDeniedHandler()));
        return http.build();
    }

    // Expone el AuthenticationManager configurado por Spring Security.
    // Puede utilizarse desde los servicios de autenticación para validar
    // las credenciales de los usuarios durante el inicio de sesión.
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Configuración CORS para controlar las solicitudes HTTP
    // realizadas desde aplicaciones frontend alojadas en otros orígenes.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedHeaders(
                Arrays.asList("Origin.Accept", "X-Requested-With", "Content-Type",
                        "Access-Control-Request-Method", "Access-Control-Request-Headers", "Authorization"));

        // Cabeceras de respuesta que podrán ser consultadas
        // desde el navegador por la aplicación frontend.
        configuration.setExposedHeaders(
                Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        configuration.setAllowedOrigins(Arrays.asList("/*"));

        // Métodos HTTP permitidos para las solicitudes CORS.
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS", "PUT", "PATCH"));
        configuration.addAllowedOrigin("*");

        // Tiempo máximo durante el cual el navegador puede reutilizar
        // la respuesta de una solicitud CORS preflight.
        configuration.setMaxAge(Duration.ZERO);

        // Permite el envío de credenciales en solicitudes CORS.
        configuration.setAllowCredentials(Boolean.TRUE);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Aplica la configuración CORS a todos los endpoints.
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
