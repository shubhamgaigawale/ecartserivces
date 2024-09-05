package com.ecart.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.time.LocalDateTime;

@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args)
	{
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Bean
	public RouteLocator ecartRouteConfig(RouteLocatorBuilder routeLocatorBuilder)
	{
		return routeLocatorBuilder.routes()
				.route(p -> p
						.path("/ecart/authservice/**")
						.filters( f -> f.rewritePath("/ecart/authservice/(?<segment>.*)","/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
						)
						.uri("lb://AUTHSERVICE"))
				.route(p -> p
						.path("/ecart/productservice/**")
						.filters( f -> f.rewritePath("/ecart/productservice/(?<segment>.*)","/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
						)
						.uri("lb://PRODUCTSERVICE")).build();
	}

}
