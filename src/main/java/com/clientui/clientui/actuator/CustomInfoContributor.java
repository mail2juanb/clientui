package com.clientui.clientui.actuator;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


/**
 * The {@code CustomInfoContributor} class is a Spring component that dynamically enriches
 * the information exposed by the {@code /actuator/info} endpoint.
 *
 * <ul>
 *   <li>Reading configuration properties defined in {@code application.properties}
 *       or other Spring configuration sources.</li>
 *   <li>Structuring and adding these properties to the JSON response of the {@code /actuator/info} endpoint.</li>
 *   <li>Adding dynamic information or business logic, such as timestamps or calculated metadata.</li>
 * </ul>
 *
 * @see InfoContributor
 * @see Environment
 */
@Component
public class CustomInfoContributor implements InfoContributor {

    private final Environment environment;

    /**
     * Constructor for the {@code CustomInfoContributor} class.
     *
     * @param environment The Spring environment, injected to read configuration properties.
     */
    public CustomInfoContributor(Environment environment) {
        this.environment = environment;
    }


    /**
     * Method called by Spring Boot Actuator to contribute information to the {@code /actuator/info} endpoint.
     *
     * <ul>
     *   <li>Retrieves configuration properties such as {@code info.app.version}
     *       and {@code info.app.description}.</li>
     *   <li>Adds dynamic information, such as the last update timestamp.</li>
     *   <li>Structures this information in a {@code Map} and adds it to the {@code Info.Builder}.</li>
     * </ul>
     *
     * @param builder The builder used to construct the final {@code Info} object.
     */
    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put("version", environment.getProperty("info.app.version", "clientui - Version not defined"));
        appInfo.put("description", environment.getProperty("info.app.description", "clientui - Description not defined"));
        appInfo.put("documentation-javadoc", environment.getProperty("info.app.documentation.javadoc", "clientui - Javadoc Documentation not defined"));
        appInfo.put("information", environment.getProperty("info.app.information", "clientui - Informations not defined"));
        appInfo.put("lastUpdated", LocalDateTime.now().toString());

        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("app", appInfo);

        builder.withDetails(infoMap);
    }
}


