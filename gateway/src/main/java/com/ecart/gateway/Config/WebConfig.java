package com.ecart.gateway.Config;

import com.ecart.gateway.Security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.WebFilter;

@Configuration
public class WebConfig implements WebFluxConfigurer {

    @Bean
    public WebFilter jwtAuthenticationFilter()
    {
        return new JwtAuthenticationFilter();
    }
}
