[🇫🇷 Lire en français](README.fr.md) | [🇬🇧 Read in English](README.md)

# clientui – Microservice Front-End de l’application MicroDiab.

---
**Projet** : MicroDiab  
**Organisation** : MediLabo Solutions  
**Auteur** : michaudj  
**Date** : Janvier 2026  
**Version** : 1.0.0
---

## 📌 Rôle
Le microservice **clientui** est le **microservice front-end** de l’application **MicroDiab**. Il joue un rôle 
d’**orchestrateur de vues** et garantit une **séparation stricte entre le front-end et la logique métier**,
conformément aux principes d’une architecture microservices.

Il est chargé :
- d’afficher les **interfaces web utilisateur** via **Thymeleaf** ;
- de gérer les **interactions utilisateur** ;
- d’orchestrer les appels aux microservices back pour l’affichage des données.

Ce microservice ne contient **aucune logique métier médicale** et ne possède **aucune base de données propre**.

**Fonctionnalités clés** :
- Affichage des pages web via **Thymeleaf**.
- Affichage des patients (liste, création, mise à jour).
- Affichage des notes médicales pour chaque patient.
- Affichage du **niveau de risque de diabète**.
- Communication inter-microservices via **Feign Client**.
- Documentation interactive via **Swagger UI** et **Javadoc**.
- Traçage distribué avec **Zipkin** et **Micrometer Tracing**.
- Containerisé avec **Docker** (compatible avec `docker-compose`).

---

## 📦 Dépendances
**Microservices Internes**

| Microservice      | Rôle                              | Port |
|-------------------|-----------------------------------|------|
| **eureka-server** | Découverte de services            | 9102 |
| **zipkin**        | Traçage distribué                 | 9411 |
| **mgateway**      | Passerelle API + Authentification | 9010 |
| **mpatient**      | Données patients                  | 9001 |
| **mnotes**        | Notes médicales                   | 9002 |
| **mrisk**         | Analyse du risque de diabète      | 9003 |

**Communication** : Feign (appels vers `mpatient`, `mnotes` et `mrisk`) est utilisé côté `clientui` 
pour consommer les API REST exposées par les microservices back, sans exposer directement ces endpoints au navigateur.  
**Tests** : Rapport JaCoCo disponible dans `target/`  
**Réseau** : microdiab-network (partagé avec les autres microservices).

**Sécurité** : La gateway applique une authentification centralisée,
mais chaque microservice reste sécurisé indépendamment afin d’éviter
toute exposition accidentelle.
* **Sécurité externe** : Spring Security (authentification centralisée via `mgateway`)
* **Sécurité interne** : `clientui` consomme les headers injectés par la gateway : `X-Auth-Username` et `X-Auth-Roles`

---

## 📊 Monitoring et Documentation
| Outil      | URL                                                                  | Description        |
|------------|----------------------------------------------------------------------|--------------------|
| Actuator   | [http://localhost:8090/actuator](http://localhost:8090/actuator)     | Métriques et santé |
| Swagger UI | [http://localhost:8090/swagger-ui](http://localhost:8090/swagger-ui) | Documentation API  |
| Zipkin     | [http://localhost:9411/zipkin](http://localhost:9411/zipkin)         | Traces distribuées |
| Javadoc    | [http://localhost:8090/apidocs](http://localhost:8090/apidocs)       | Documentation Java |

---

## 🛠 Prérequis
- Java 21
- Spring Boot 3.5.8
- Spring Cloud 2025.0.0
- Maven
- Docker
- Variables d'environnement :
    - `EUREKA_SERVER_HOST=eureka-server`
    - `EUREKA_SERVER_PORT=9102`

---

## 🚀 Endpoints principaux
| Méthode | Endpoint                     | Description                      |
|---------|------------------------------|----------------------------------|
| GET     | `/home`                      | Page d’accueil                   |
| GET     | `/patients`                  | Liste des patients               |
| GET     | `/add`                       | Formulaire d’ajout de patient    |
| POST    | `/add/addPatient`            | Création d’un patient            |
| GET     | `/update/{id}`               | Page de mise à jour d’un patient |
| POST    | `/update/{id}/updatepatient` | Mise à jour d’un patient         |
| POST    | `/update/{id}/addnotes`      | Ajout d’une note médicale        |

---
