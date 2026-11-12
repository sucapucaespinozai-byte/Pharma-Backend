package com.example.tienda.config;

import com.example.tienda.enums.Permiso;
import com.example.tienda.security.JwtAuthenticationFilter;
import com.example.tienda.security.JwtService;
import com.example.tienda.security.RestAccessDeniedHandler;
import com.example.tienda.security.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
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
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final String[] PUBLIC_ROUTES = {
            "/api/v1/auth/login",
            "/actuator/health", "/actuator/health/**",
            "/api/v1/health",
            "/api/v1/health/**",
            "/api/health",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/error"
    };

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtService jwtService,
            UserDetailsService userDetailsService,
            CorsConfigurationSource corsConfigurationSource,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ROUTES).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/categorias/**",
                                "/api/v1/productos/**")
                        .hasAuthority(Permiso.CATALOGO_LEER.name())
                        .requestMatchers("/api/v1/categorias/**", "/api/v1/productos/**")
                        .hasAuthority(Permiso.CATALOGO_ESCRIBIR.name())
                        .requestMatchers(HttpMethod.GET, "/api/v1/clientes/**")
                        .hasAuthority(Permiso.CLIENTES_LEER.name())
                        .requestMatchers("/api/v1/clientes/**")
                        .hasAuthority(Permiso.CLIENTES_ESCRIBIR.name())
                        .requestMatchers(HttpMethod.POST, "/api/v1/ventas")
                        .hasAuthority(Permiso.VENTAS_REGISTRAR.name())
                        .requestMatchers(HttpMethod.GET, "/api/v1/ventas/**")
                        .hasAuthority(Permiso.VENTAS_LEER.name())
                        .requestMatchers("/api/v1/ventas/**")
                        .hasAuthority(Permiso.VENTAS_REGISTRAR.name())
                        .requestMatchers(
                                "/api/v1/usuarios/**",
                                "/api/v1/empleados/**",
                                "/api/v1/roles/**", "/api/v1/auditoria/**")
                        .hasAuthority(Permiso.USUARIOS_ADMINISTRAR.name())
                        .requestMatchers("/actuator", "/actuator/**")
                        .hasAuthority(Permiso.USUARIOS_ADMINISTRAR.name())
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
