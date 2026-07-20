package com.doxabeta.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Wires up authentication (who are you?) and authorization (what are you
 * allowed to do?) for the whole API, plus CORS (which browser origins are
 * allowed to call this API at all).
 *
 * AUTHENTICATION: HTTP Basic — every request must include an
 * "Authorization: Basic base64(username:password)" header. There are three
 * hardcoded demo usernames (admin/mentor/student); their passwords come from
 * the ADMIN_PASSWORD / MENTOR_PASSWORD / STUDENT_PASSWORD environment
 * variables (see .env.example), falling back to admin123 / mentor123 /
 * student123 if those env vars aren't set (see application.yml).
 *
 * AUTHORIZATION rules (checked top-to-bottom, first match wins):
 *  - /h2-console/**, /swagger-ui/**, /v3/api-docs/**  -> public, no login needed (local dev tooling)
 *  - /api/admin/**                                     -> ADMIN only
 *  - GET  /api/**                                       -> ADMIN, MENTOR, STUDENT
 *  - POST /api/daily-hours, POST /api/assignments       -> ADMIN, MENTOR, STUDENT (students act on their own behalf)
 *  - any other POST/PUT under /api/**                   -> ADMIN, MENTOR only
 *  - anything else                                      -> must at least be logged in
 *
 * See README.md's "Access Control" section for the same table in one place,
 * and "How to Test" for example curl commands exercising each role.
 */
@Configuration
public class SecurityConfig {

    // @Value pulls these from application.yml, which in turn reads them from
    // environment variables with a fallback default — see application.yml's
    // `app.security.*` and `app.cors.*` keys.
    @Value("${app.security.admin-password}")
    private String adminPassword;

    @Value("${app.security.mentor-password}")
    private String mentorPassword;

    @Value("${app.security.student-password}")
    private String studentPassword;

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    /**
     * BCrypt is the industry-standard algorithm for hashing passwords before
     * storing/comparing them — even though these are just in-memory demo
     * users, using a real encoder means the pattern here is copy-pasteable
     * into a production setup backed by a real user database later.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the three demo accounts Spring Security checks incoming
     * Basic-auth credentials against. InMemoryUserDetailsManager is exactly
     * what it sounds like — no database table backs these users; they're
     * (re)created fresh from the configured passwords every time the app starts.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        var admin = User.withUsername("admin")
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN")
                .build();
        var mentor = User.withUsername("mentor")
                .password(passwordEncoder.encode(mentorPassword))
                .roles("MENTOR")
                .build();
        var student = User.withUsername("student")
                .password(passwordEncoder.encode(studentPassword))
                .roles("STUDENT")
                .build();
        return new InMemoryUserDetailsManager(admin, mentor, student);
    }

    /**
     * The core security setup: builds the chain of filters every HTTP request
     * passes through before it reaches a controller.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protection is for browser-based, cookie-authenticated apps
                // where a malicious site could trick a logged-in browser into
                // submitting a form. This API uses stateless per-request Basic
                // auth (no cookies/sessions), so CSRF protection doesn't apply
                // and would only get in the way of API clients (curl, Webflow,
                // Postman, etc.) and the H2 console.
                .csrf(csrf -> csrf.disable())
                // Delegates to the corsConfigurationSource() bean defined below.
                .cors(Customizer.withDefaults())
                // The H2 console renders itself inside an HTML <frame>. Browsers
                // block frames by default (X-Frame-Options: DENY) unless the
                // framed page is from the same origin — sameOrigin() allows that
                // without opening the app up to being framed by other sites.
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .authorizeHttpRequests(auth -> auth
                        // Local dev tooling: H2's web console and the Swagger/OpenAPI UI.
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // Admin-only endpoints (overview, raw data dump, manual re-seed).
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Anyone logged in (any of the three roles) can read data.
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                        // Students are allowed to log their own hours and submit
                        // their own assignments — these two POST routes are the
                        // exception to "students are read-only".
                        .requestMatchers(HttpMethod.POST, "/api/daily-hours").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                        .requestMatchers(HttpMethod.POST, "/api/assignments").hasAnyRole("ADMIN", "MENTOR", "STUDENT")
                        // Every other write (creating/updating students, mentors,
                        // cohorts, reviews, grading assignments) requires staff privileges.
                        .requestMatchers(HttpMethod.POST, "/api/**").hasAnyRole("ADMIN", "MENTOR")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasAnyRole("ADMIN", "MENTOR")
                        // Fallback: anything not explicitly matched above still
                        // requires at least being logged in as one of the three roles.
                        .anyRequest().authenticated()
                )
                // Enables HTTP Basic auth with default settings (browsers will
                // show their native login prompt; API clients send the
                // Authorization header directly).
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    /**
     * Defines which browser origins (protocol+domain+port) are allowed to call
     * this API via JavaScript fetch()/XHR — needed so a frontend like Webflow,
     * hosted on a different domain than the backend, isn't blocked by the
     * browser's same-origin policy.
     *
     * Controlled by the CORS_ALLOWED_ORIGINS environment variable
     * (see .env.example): "*" (the default) allows any origin, which is
     * convenient during development but should be tightened to your actual
     * frontend's domain (e.g. "https://your-site.webflow.io") before a real
     * deployment — see README.md's "Access Control" section.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        if ("*".equals(allowedOrigins.trim())) {
            // setAllowedOriginPatterns (not setAllowedOrigins) is required for
            // the wildcard case in Spring Security 6 when allowCredentials is
            // involved — using the patterns variant here keeps this working
            // correctly regardless of the allowCredentials setting below.
            configuration.setAllowedOriginPatterns(List.of("*"));
        } else {
            // Supports a comma-separated list, e.g.
            // "https://mysite.webflow.io,https://staging.mysite.com"
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        }
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        // false because this API doesn't use cookies for auth (it uses the
        // Authorization header via HTTP Basic), so there's no session cookie
        // that needs cross-origin credential support.
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
