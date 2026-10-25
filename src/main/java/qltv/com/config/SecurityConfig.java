package qltv.com.config;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<AccountStatusFilter> accountStatusRegistration(AccountStatusFilter filter) {
        var registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
        registration.setEnabled(false); return registration;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AccountStatusFilter accountStatusFilter) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/auth/**", "/css/**", "/js/**", "/images/**", "/books/**").permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/user/**").hasRole("USER")
            .anyRequest().denyAll())
            .formLogin(form -> form.loginPage("/auth/login").loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/auth/redirect", true).failureUrl("/auth/login?error=true").permitAll())
            .logout(logout -> logout.logoutUrl("/auth/logout").logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true).clearAuthentication(true));
        http.addFilterAfter(accountStatusFilter, org.springframework.security.web.authentication.AnonymousAuthenticationFilter.class);
        return http.build();
    }
}
