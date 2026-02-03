package muse.back.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients(basePackages = "auth.common.core.client")
@EnableDiscoveryClient
public class MuseBackServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MuseBackServiceApplication.class, args);
    }

}
