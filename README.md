
[🇫🇷 Read in French](README.fr.md) | [🇬🇧 Read in English](README.md)

# clientui – MicroDiab Application Front-End Microservice

---
**Project**: MicroDiab  
**Organization**: MediLabo Solutions  
**Author**: michaudj  
**Date**: January 2026  
**Version**: 1.0.0
---

## 📌 Role
The **clientui** microservice is the **front-end microservice** of the **MicroDiab** application. 
It acts as a **view orchestrator** and ensures a **strict separation between the front-end and business logic**, 
in accordance with microservices architecture principles.

It is responsible for:
- Displaying **user web interfaces** via **Thymeleaf**.;
- Managing **user interactions**.;
- Orchestrating calls to back-end microservices for data display.

This microservice contains **no medical business logic** and has **no database of its own**.

**Key Features**:
- Displaying web pages via **Thymeleaf**.;
- Displaying patients (list, creation, update).;
- Displaying medical notes for each patient.;
- Displaying the **diabetes risk level**.;
- Inter-microservice communication via **Feign Client**.;
- Interactive documentation via **Swagger UI** and **Javadoc**.;
- Distributed tracing with **Zipkin** and **Micrometer Tracing**.;
- Containerized with **Docker** (compatible with `docker-compose`).

---

## 📦 Dependencies
**Internal Microservices**

| Microservice      | Role                              | Port |
|-------------------|-----------------------------------|------|
| **eureka-server** | Service discovery                 | 9102 |
| **zipkin**        | Distributed tracing               | 9411 |
| **mgateway**      | API Gateway + Authentication      | 9010 |
| **mpatient**      | Patient data                      | 9001 |
| **mnotes**        | Medical notes                     | 9002 |
| **mrisk**         | Diabetes risk analysis            | 9003 |

**Communication**: Feign (calls to `mpatient`, `mnotes`, and `mrisk`) is used on the `clientui` side to consume the REST APIs exposed by the back-end microservices, without directly exposing these endpoints to the browser.
**Tests**: JaCoCo report available in `target/`
**Network**: microdiab-network (shared with other microservices).

**Security**: The gateway applies centralized authentication, but each microservice remains independently secured to prevent accidental exposure.
- **External Security**: Spring Security (centralized authentication via `mgateway`)
- **Internal Security**: `clientui` consumes headers injected by the gateway: `X-Auth-Username` and `X-Auth-Roles`

---

## 📊 Monitoring and Documentation
| Tool       | URL                                                                  | Description        |
|------------|----------------------------------------------------------------------|--------------------|
| Actuator   | [http://localhost:8090/actuator](http://localhost:8090/actuator)     | Metrics and health |
| Swagger UI | [http://localhost:8090/swagger-ui](http://localhost:8090/swagger-ui) | API Documentation  |
| Zipkin     | [http://localhost:9411/zipkin](http://localhost:9411/zipkin)         | Distributed traces |
| Javadoc    | [http://localhost:8090/apidocs](http://localhost:8090/apidocs)       | Java Documentation |

---

## 🛠 Prerequisites
- Java 21
- Spring Boot 3.5.8
- Spring Cloud 2025.0.0
- Maven
- Docker
- Environment variables:
    - `EUREKA_SERVER_HOST=eureka-server`
    - `EUREKA_SERVER_PORT=9102`

---

## 🚀 Main Endpoints
| Method | Endpoint                     | Description                      |
|--------|------------------------------|----------------------------------|
| GET    | `/home`                      | Home page                        |
| GET    | `/patients`                  | List of patients                 |
| GET    | `/add`                       | Patient addition form            |
| POST   | `/add/addPatient`            | Create a patient                 |
| GET    | `/update/{id}`               | Patient update page              |
| POST   | `/update/{id}/updatepatient` | Update a patient                 |
| POST   | `/update/{id}/addnotes`      | Add a medical note               |

---
