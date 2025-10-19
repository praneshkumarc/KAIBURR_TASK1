package com.example.taskrunner.config;


import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KubernetesConfig {
    @Bean
    @ConditionalOnProperty(prefix = "runner", name = "mode", havingValue = "k8s", matchIfMissing = true)
    public KubernetesClient kubernetesClient() {
        return new KubernetesClientBuilder().build();
    }
}
