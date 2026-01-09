package com.clientui.clientui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


/**
 * Main application class for the *ClientUI* microservice in the *MicroDiab* project.
 * This class serves as the entry point for the Spring Boot application and enables:
 * <ul>
 *   <li>Service discovery with Eureka (via {@link EnableDiscoveryClient}).</li>
 *   <li>Declarative REST clients with Feign (via {@link EnableFeignClients}).</li>
 * </ul>
 *
 * <p>This microservice communicates with the *mpatient* backend using Feign clients and is
 * registered with the *mgateway* microservice for authentication and routing.</p>
 *
 * @see SpringApplication
 * @see EnableFeignClients
 * @see EnableDiscoveryClient
 */
@SpringBootApplication
@EnableFeignClients("com.clientui")
@EnableDiscoveryClient
public class ClientuiApplication {

    /**
     * Main method to start the *ClientUI* microservice.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(ClientuiApplication.class, args);
    }

}
