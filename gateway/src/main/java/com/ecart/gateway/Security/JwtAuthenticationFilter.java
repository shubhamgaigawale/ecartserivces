package com.ecart.gateway.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class JwtAuthenticationFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Log request URL to check if filter is being invoked
        logger.info("Request URL: " + request.getURI());

        if (request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    // Decode secret key (if Base64-encoded)
                    //yte[] decodedSecretKey = Base64.getDecoder().decode(secretKey.getBytes(StandardCharsets.UTF_8));

                    Claims claims = Jwts.parser()
                            .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8)) // Ensure this matches the signing key
                            .parseClaimsJws(token)
                            .getBody();

                    String username = claims.getSubject();
                    List<String> roles = claims.get("roles", List.class);

                    // Log token details
                    logger.info("Parsed Token Claims: " + claims);
                    logger.info("Username from Token: " + username);
                    logger.info("Roles from Token: " + roles);

                    // Add the claims or any needed info to the request headers for downstream services
                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                            .header("X-Authenticated-User", username)
                            .header("X-Authenticated-Roles", roles.toString())
                            .build();

                    exchange = exchange.mutate().request(modifiedRequest).build();

                } catch (ExpiredJwtException e) {
                    logger.error("Token expired: " + e.getMessage());
                    return Mono.error(new RuntimeException("JWT Token has expired"));
                } catch (JwtException e) {
                    logger.error("Invalid JWT: " + e.getMessage());
                    return Mono.error(new RuntimeException("Invalid JWT Token"));
                } catch (Exception e) {
                    logger.error("Error parsing JWT: " + e.getMessage());
                    return Mono.error(new RuntimeException("JWT parsing error"));
                }
            } else {
                logger.error("Authorization header does not start with Bearer");
            }
        }

        return chain.filter(exchange);
    }

}
