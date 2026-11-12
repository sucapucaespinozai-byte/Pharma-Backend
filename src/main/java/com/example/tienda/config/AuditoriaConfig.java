package com.example.tienda.config;
import java.util.Optional;
import org.springframework.context.annotation.*;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
@Configuration
@EnableJpaAuditing(auditorAwareRef="auditor")
public class AuditoriaConfig {
 @Bean public AuditorAware<String> auditor() {
  return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
   .filter(a->a.isAuthenticated() && !(a instanceof AnonymousAuthenticationToken))
   .map(a->a.getName()).or(()->Optional.of("sistema"));
 }
}
