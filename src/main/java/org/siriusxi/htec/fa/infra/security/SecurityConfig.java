package org.siriusxi.htec.fa.infra.security;

import lombok.extern.slf4j.Slf4j;
import org.siriusxi.htec.fa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static java.lang.String.format;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static org.springframework.security.core.context.SecurityContextHolder.MODE_INHERITABLETHREADLOCAL;
import static org.springframework.security.core.context.SecurityContextHolder.setStrategyName;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true
)
public class SecurityConfig {
    
    private final UserRepository userRepository;
    private final JwtTokenFilter jwtTokenFilter;
    private final String appVersion;
    
    public SecurityConfig(UserRepository userRepository,
                          JwtTokenFilter jwtTokenFilter,
                          @Value("${app.version:v1}") String appVersion) {
        this.userRepository = userRepository;
        this.jwtTokenFilter = jwtTokenFilter;
        this.appVersion = "/".concat(appVersion);
        
        // Inherit security context in async function calls
        setStrategyName(MODE_INHERITABLETHREADLOCAL);
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(
                    () -> new UsernameNotFoundException(
                        format("User: %s, not found", username)));
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    // Set password encoding schema
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // Security configurations
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        // List of Swagger URLs
        var swaggerAuthList = new String[]{
            appVersion.concat("/api-docs/**"),
            "/webjars/**", "/swagger-ui/**",
            appVersion.concat("/doc/**")};
        
        http
            // Enable CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            //Disable CSRF
            .csrf(AbstractHttpConfigurer::disable)
            
            // Set session management to stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Set unauthorized requests exception handler
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, ex) -> {
                    log.error("Unauthorized request - {}", ex.getMessage());
                    response.sendError(SC_UNAUTHORIZED, ex.getMessage());
                }))
            
            // Set H2 database console permission and frame options
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
            
            // Set permissions on endpoints
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html").permitAll()
                .requestMatchers("/db-console/**").permitAll()
                // Swagger endpoints must be publicly accessible
                .requestMatchers(swaggerAuthList).permitAll()
                // Our public endpoints
                .requestMatchers("/public/**").permitAll()
                // Allow the version/info endpoint to be public so health/version checks don't require auth
                .requestMatchers("/v1/info/**").permitAll()
                .requestMatchers(appVersion.concat("/info/**")).permitAll()
                //Our private endpoints
                .anyRequest().authenticated())
            
            // Add JWT token filter
            .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
    
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        var source = new UrlBasedCorsConfigurationSource();
        var config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    
    // Used by spring security if CORS is enabled.
    @Bean
    public CorsFilter corsFilter() {
        return new CorsFilter(corsConfigurationSource());
    }
    
    // Expose authentication manager bean
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
