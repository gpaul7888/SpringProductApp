package com.company.project.product.config;

import com.company.project.product.client.UserClient;
import java.net.http.HttpClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * HTTP clients for service-to-service calls.
 *
 * <p>Beans marked {@link LoadBalanced} resolve {@code http://<service-name>} through Eureka.
 * A separate, plain {@link Primary} builder is kept for everything else, so libraries that
 * inject a {@code RestClient.Builder} are not accidentally routed through the load balancer.
 */
@Configuration(proxyBeanMethods = false)
public class HttpClientConfig {

    @Bean
    @Primary
    @Scope("prototype")
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @LoadBalanced
    @Scope("prototype")
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    /** Spring Security's JWKS downloader still needs a {@code RestOperations}, hence a RestTemplate. */
    @Bean
    @LoadBalanced
    RestTemplate loadBalancedRestTemplate(AuthServiceProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.connectTimeout());
        factory.setReadTimeout(properties.readTimeout());
        return new RestTemplate(factory);
    }

    /**
     * Declarative HTTP client: Spring generates the implementation of {@link UserClient}
     * from its annotations - the same idea as Feign, but built into Spring Framework.
     */
    @Bean
    UserClient userClient(@LoadBalanced RestClient.Builder builder, AuthServiceProperties properties) {
        RestClient restClient = builder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory(properties))
                .build();
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build()
                .createClient(UserClient.class);
    }

    private static ClientHttpRequestFactory requestFactory(AuthServiceProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(properties.readTimeout());
        return factory;
    }
}
