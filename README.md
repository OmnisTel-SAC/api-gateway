# API Gateway

Puerta de entrada única al sistema OmnisTel.
Enruta las peticiones a los microservicios correspondientes,
valida tokens JWT y aplica rate limiting por IP.

## Tecnologías

- Java 17
- Spring Boot 3.x
- Spring Cloud Gateway (WebFlux)
- Spring Security (OAuth2 Resource Server)
- JWT (RSA-256)
- Redis (rate limiting)
- Eureka Discovery Client
- Spring Cloud Config Client
- OpenAPI / Swagger

## Estructura

```
api-gateway/
├── src/
│   ├── main/
│   │   ├── java/com/omnistel/apigateway/
│   │   │   └── config/
│   │   │       ├── JwtAuthFilter.java
│   │   │       ├── RateLimiterConfig.java
│   │   │       └── SecurityConfig.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── bootstrap.yml
│   └── test/
│       ├── java/.../apigateway/
│       │   ├── ApiGatewayApplicationTests.java
│       │   └── config/JwtAuthFilterTest.java
│       └── resources/application.yml
├── Dockerfile
├── pom.xml
└── .gitignore
```

## Patrones de Diseño

| Patrón | Descripción |
|--------|-------------|
| **API Gateway Pattern** | Punto único de entrada que enruta a microservicios |
| **Filter Chain** | Filtros de autenticación JWT aplicados antes del enrutamiento |
| **Token Bucket** | Rate limiting por IP para login y registro |
| **Route Pattern** | Enrutamiento basado en paths hacia servicios internos |

## Infraestructura

| Componente | Uso |
|------------|-----|
| **Redis** | Rate limiting (token bucket por IP) |
| **Eureka** | Descubrimiento dinámico de servicios |
| **Config Server** | Configuración centralizada desde classpath (modo native) |

## Rutas

| Ruta | Destino | Autenticación |
|------|---------|---------------|
| `/api/auth/**` | `auth-service:8081` | Público (login, register) |
| `/api/tickets/**` | `ticket-service:8082` | JWT |
| `/api/notifications/**` | `notification-service:8083` | JWT |

## Rate Limiting

| Endpoint | Rate | Burst |
|----------|------|-------|
| `/api/auth/login` | 1 request/s | 10 |
| `/api/auth/register` | 1 request/s | 3 |

## Puerto

- `8050` (expuesto al exterior)

## Dependencias

- **Auth Service** — validación de tokens JWT
- **Ticket Service** — enrutamiento de peticiones de tickets
- **Notification Service** — enrutamiento de peticiones de notificaciones
- **Config Server** — configuración centralizada
- **Eureka Server** — descubrimiento de servicios
- **Redis** — rate limiting
