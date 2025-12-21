package com.clientui.clientui.actuator;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


/*  La classe CustomInfoContributor est un composant Spring qui enrichit dynamiquement
    les informations exposées par l'endpoint /actuator/info
    - Elle lit les propriétés définies dans application.properties (ou d'autres sources de configuration).
    - Elle les structure et les ajoute au JSON retourné par /actuator/info.
    - Elle permet d'ajouter des logiques métiers ou des informations dynamiques
      (ex: calculer une version basée sur un commit Git, ajouter des métadonnées spécifiques).*/

@Component
public class CustomInfoContributor implements InfoContributor {

    private final Environment environment;

    public CustomInfoContributor(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put("version", environment.getProperty("info.app.version", "clientui - Version non définie"));
        appInfo.put("description", environment.getProperty("info.app.description", "clientui - Description non définie"));
        appInfo.put("documentation", environment.getProperty("info.app.documentation", "clientui - Documentation non définie"));
        appInfo.put("information", environment.getProperty("info.app.information", "clientui - Informations non définies"));
        // Ajout d'une info dynamique (ex: horodatage)
        appInfo.put("lastUpdated", LocalDateTime.now().toString());

        Map<String, Object> infoMap = new HashMap<>();
        infoMap.put("app", appInfo);

        builder.withDetails(infoMap);
    }
}