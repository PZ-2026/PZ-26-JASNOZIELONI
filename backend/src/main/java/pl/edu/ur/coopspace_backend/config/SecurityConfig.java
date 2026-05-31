package pl.edu.ur.coopspace_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures stateless JWT-based security for the REST API.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Creates security configuration.
     *
     * @param jwtAuthFilter JWT authentication filter
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }
    
    /**
     * Provides password encoder bean.
     *
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures HTTP security for JWT-based stateless authentication.
     *
     * @param http HTTP security builder
     * @return configured security filter chain
     * @throws Exception when security chain building fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // wylaczenie CSRF, poniewaz w REST API nie jest potrzebne i blokuje zapytania POST
                .csrf(AbstractHttpConfigurer::disable)
                // konfiguracja uprawnien do sciezek
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/login").permitAll() // publiczny jest tylko login
                        .anyRequest().authenticated() // pozostale zapytania wymagaja autoryzacji
                )
                // ustawiamy sesje na bezstanowa, czyli typowa dla API z JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
