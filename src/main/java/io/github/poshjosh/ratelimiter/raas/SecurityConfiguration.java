package io.github.poshjosh.ratelimiter.raas;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

// See https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
// See https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html#authentication-password-storage-boot-cli
@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final Environment environment;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (environment.getProperty("security.user.name") == null
                || environment.getProperty("security.user.password") == null) {
            return http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }
        return http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(withDefaults())
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = {"security.user.name", "security.user.password"})
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username(environment.getRequiredProperty("security.user.name"))
                .password(environment.getRequiredProperty("security.user.password"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}