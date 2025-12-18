package com.robotech.robotech_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Ya NO usamos el RolInterceptor porque ahora JWT manejará los roles
}
