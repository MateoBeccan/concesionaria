package com.concesionaria.app.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.concesionaria.app.security.*;
import com.concesionaria.app.web.filter.SpaWebFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import tech.jhipster.config.JHipsterProperties;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private static final String[] ADMIN_MANAGED_API_PATHS = {
        "/api/carrocerias",
        "/api/carrocerias/**",
        "/api/combustibles",
        "/api/combustibles/**",
        "/api/condicion-ivas",
        "/api/condicion-ivas/**",
        "/api/cotizacions",
        "/api/cotizacions/**",
        "/api/marcas",
        "/api/marcas/**",
        "/api/metodo-pagos",
        "/api/metodo-pagos/**",
        "/api/modelos",
        "/api/modelos/**",
        "/api/monedas",
        "/api/monedas/**",
        "/api/motors",
        "/api/motors/**",
        "/api/tipo-cajas",
        "/api/tipo-cajas/**",
        "/api/tipo-comprobantes",
        "/api/tipo-comprobantes/**",
        "/api/tipo-documentos",
        "/api/tipo-documentos/**",
        "/api/tipo-vehiculos",
        "/api/tipo-vehiculos/**",
        "/api/traccions",
        "/api/traccions/**",
        "/api/ubicacion-stocks",
        "/api/ubicacion-stocks/**",
        "/api/versions",
        "/api/versions/**"
    };

    private final JHipsterProperties jHipsterProperties;
    private final boolean publicRegistrationEnabled;

    public SecurityConfiguration(
        JHipsterProperties jHipsterProperties,
        @Value("${app.security.registration.public-enabled:false}") boolean publicRegistrationEnabled
    ) {
        this.jHipsterProperties = jHipsterProperties;
        this.publicRegistrationEnabled = publicRegistrationEnabled;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .cors(withDefaults())
            .csrf(csrf -> csrf.disable())
            .addFilterAfter(new SpaWebFilter(), BasicAuthenticationFilter.class)
            .headers(headers ->
                headers
                    .contentSecurityPolicy(csp -> csp.policyDirectives(jHipsterProperties.getSecurity().getContentSecurityPolicy()))
                    .frameOptions(FrameOptionsConfig::sameOrigin)
                    .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                    .permissionsPolicyHeader(permissions ->
                        permissions.policy(
                            "camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()"
                        )
                    )
            )
            .authorizeHttpRequests(authz -> {
                // prettier-ignore
                authz
                    .requestMatchers("/index.html", "/*.js", "/*.txt", "/*.json", "/*.map", "/*.css").permitAll()
                    .requestMatchers("/*.ico", "/*.png", "/*.svg", "/*.webapp").permitAll()
                    .requestMatchers("/assets/**").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/authenticate").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/authenticate").permitAll()
                    .requestMatchers("/api/activate").permitAll()
                    .requestMatchers("/api/account/reset-password/init").permitAll()
                    .requestMatchers("/api/account/reset-password/finish").permitAll();
                if (publicRegistrationEnabled) {
                    authz.requestMatchers("/api/register").permitAll();
                }
                authz
                    .requestMatchers("/api/admin/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.POST, ADMIN_MANAGED_API_PATHS).hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.PUT, ADMIN_MANAGED_API_PATHS).hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.PATCH, ADMIN_MANAGED_API_PATHS).hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.DELETE, ADMIN_MANAGED_API_PATHS).hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.POST, "/api/comprobantes").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.PUT, "/api/comprobantes/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.PATCH, "/api/comprobantes/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.DELETE, "/api/comprobantes/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.DELETE, "/api/ventas/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.POST, "/api/vehiculos").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.PUT, "/api/vehiculos/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.PATCH, "/api/vehiculos/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.DELETE, "/api/vehiculos/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.POST, "/api/inventarios").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.PUT, "/api/inventarios/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.PATCH, "/api/inventarios/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers(HttpMethod.DELETE, "/api/inventarios/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers("/api/**").authenticated()
                    .requestMatchers("/v3/api-docs/**").hasAuthority(AuthoritiesConstants.ADMIN)
                    .requestMatchers("/management/health").permitAll()
                    .requestMatchers("/management/health/**").permitAll()
                    .requestMatchers("/management/info").permitAll()
                    .requestMatchers("/management/prometheus").permitAll()
                    .requestMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN);
            })
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions ->
                exceptions
                    .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                    .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        return http.build();
    }
}
