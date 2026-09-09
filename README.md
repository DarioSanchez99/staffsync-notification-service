<div align="center">

# StaffSync — Notification Service

**Notificaciones en tiempo real consumidas desde RabbitMQ**

![Java](https://img.shields.io/badge/Java%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot%203.3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)

</div>

---

Microservicio que escucha el exchange RabbitMQ `staffsync.notifications` y persiste las notificaciones para cada usuario. Expone una API REST para que el frontend consulte las notificaciones no leídas y las marque como leídas.

---

## Tipos de notificación

`VACATION_APPROVED` · `VACATION_REJECTED` · `SCHEDULE_UPDATED` · `GENERAL`

---

## API endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/notifications` | Notificaciones del usuario actual (X-User-Id) |
| GET | `/notifications/unread/count` | Número de notificaciones no leídas |
| PUT | `/notifications/{id}/read` | Marcar como leída |
| PUT | `/notifications/read-all` | Marcar todas como leídas |

---

## RabbitMQ

| Parámetro | Valor |
|---|---|
| Exchange | `staffsync.notifications` (topic) |
| Cola | `staffsync.vacation.notifications` |
| Routing key | `vacation.#` |

---

## Tecnologías

| Capa | Tecnología |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Mensajería | RabbitMQ (Spring AMQP) |
| ORM | Spring Data JPA |
| Base de datos | PostgreSQL / H2 |
| Mapeo | MapStruct 1.5.5 |
| API spec | OpenAPI Generator 7.7.0 |

---

## Variables de entorno

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/staffsync_notification` | Conexión PostgreSQL |
| `DB_USER` / `DB_PASS` | `staffsync` | Credenciales |
| `RABBITMQ_HOST` | `localhost` | Host de RabbitMQ |
| `RABBITMQ_USER` / `RABBITMQ_PASS` | `guest` | Credenciales RabbitMQ |
| `EUREKA_URL` | `http://admin:admin@localhost:8761/eureka/` | URL de Eureka |

---

## Tests

```bash
mvn test
```

3 tests unitarios (listar notificaciones, marcar como leída, contar no leídas).

---

## Ejecución local

```bash
mvn spring-boot:run
```

Servicio disponible en: `http://localhost:8085`

---

## Parte de StaffSync

Ver [staffsync](https://github.com/DarioSanchez99/staffsync) para el índice completo del proyecto.
