package com.example.changeadvisor;

import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
import org.springframework.ai.mcp.customizer.McpClientCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration(proxyBeanMethods = false)
class AdvisorMcpConfiguration {
  private final String authorization;

  AdvisorMcpConfiguration(@Value("${advisor.mcp.token}") String token) {
    authorization = "Bearer " + (token.isBlank() ? UUID.randomUUID().toString() : token);
  }

  @Bean
  McpClientCustomizer<HttpClientStreamableHttpTransport.Builder> advisorMcpClientAuthorization() {
    return (name, builder) ->
        builder.requestBuilder(
            HttpRequest.newBuilder().header(HttpHeaders.AUTHORIZATION, authorization));
  }

  @Bean
  @Order(1)
  SecurityFilterChain advisorMcpFilterChain(HttpSecurity http) {
    var endpoint = PathPatternRequestMatcher.withDefaults().matcher("/mcp");
    http.securityMatcher(endpoint)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .csrf(AbstractHttpConfigurer::disable)
        .exceptionHandling(
            errors ->
                errors.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .authorizeHttpRequests(
            registry ->
                registry
                    .anyRequest()
                    .access(
                        (authentication, context) -> {
                          String header = context.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                          return new AuthorizationDecision(
                              header != null
                                  && MessageDigest.isEqual(
                                      authorization.getBytes(StandardCharsets.UTF_8),
                                      header.getBytes(StandardCharsets.UTF_8)));
                        }));
    return http.build();
  }
}
