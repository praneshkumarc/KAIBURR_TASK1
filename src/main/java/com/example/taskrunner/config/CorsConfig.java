// config/CorsConfig.java
package com.example.taskrunner.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class CorsConfig implements WebMvcConfigurer {
@Value("${app.cors.allowedOrigin}")
private String allowedOrigin;


@Override public void addCorsMappings(CorsRegistry reg){
reg.addMapping("/api/**")
.allowedOrigins(allowedOrigin)
.allowedMethods("GET","PUT","DELETE")
.allowCredentials(false)
.maxAge(3600);
}
}