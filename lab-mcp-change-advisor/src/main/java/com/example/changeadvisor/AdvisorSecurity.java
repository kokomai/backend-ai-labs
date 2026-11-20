package com.example.changeadvisor;

import static org.springframework.security.config.Customizer.withDefaults;

import jakarta.servlet.DispatcherType;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
class AdvisorSecurity {
  @Bean
  PasswordEncoder advisorPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  @DependsOnDatabaseInitialization
  JdbcUserDetailsManager advisorUsers(
      DataSource dataSource,
      PlatformTransactionManager transactions,
      PasswordEncoder encoder,
      @Value("${advisor.accounts.biz-password}") String bizPassword,
      @Value("${advisor.accounts.dev-password}") String devPassword) {
    var users = new JdbcUserDetailsManager(dataSource);
    new TransactionTemplate(transactions)
        .executeWithoutResult(
            status -> {
              createAccount(users, encoder, "biz", bizPassword, "BUSINESS");
              createAccount(users, encoder, "dev", devPassword, "DEVELOPER");
            });
    return users;
  }

  private void createAccount(
      JdbcUserDetailsManager users,
      PasswordEncoder encoder,
      String username,
      String password,
      String role) {
    if (!users.userExists(username)) {
      users.createUser(
          User.withUsername(username).password(encoder.encode(password)).roles(role).build());
    }
  }

  @Bean
  SecurityFilterChain advisorFilterChain(HttpSecurity http) {
    var api = PathPatternRequestMatcher.withDefaults().matcher("/api/**");
    http.authorizeHttpRequests(
            rules ->
                rules
                    .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD)
                    .permitAll()
                    .requestMatchers("/api/csrf", "/favicon.ico")
                    .permitAll()
                    .requestMatchers("/api/business/**", "/index.html", "/business.js")
                    .hasRole("BUSINESS")
                    .requestMatchers("/api/developer/**", "/developer.html", "/developer.js")
                    .hasRole("DEVELOPER")
                    .requestMatchers("/", "/advisor.css", "/common.js", "/api/screens")
                    .authenticated()
                    .anyRequest()
                    .denyAll())
        .exceptionHandling(
            errors ->
                errors
                    .defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), api)
                    .defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"), AnyRequestMatcher.INSTANCE))
        .formLogin(form -> form.defaultSuccessUrl("/", true).permitAll())
        .logout(withDefaults());
    return http.build();
  }
}
