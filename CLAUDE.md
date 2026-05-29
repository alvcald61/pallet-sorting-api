# pallet-sorting-api — Backend

Spring Boot 3.5 / Java 21 REST API for the TUPACK pallet logistics platform.

## Commands

```bash
# Run the application
mvn spring-boot:run

# Build (skip tests)
mvn clean package -DskipTests

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=ClassName
```

## Architecture

Hexagonal (ports & adapters) per module. Each top-level module follows:

```
<module>/
  domain/          # Entities, enums, value objects, domain exceptions
  application/     # Use cases: services, DTOs, mappers
  infrastructure/
    inbound/controller/   # REST controllers
    outbound/database/    # JPA repositories
    outbound/storage/     # File storage adapters
```

Top-level modules: `common`, `user`, `order`, `invoice`, `notification`, `configuration`.

## Key Conventions

### Entities
- Extend `BaseEntity` (provides `id`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`, `enabled`, `@PrePersist`/`@PreUpdate`).
- Soft delete: set `enabled = false`, never physically delete rows.
- Custom entities not extending `BaseEntity` must define their own `@PrePersist`/`@PreUpdate`.

### Controllers
- Annotated with `@RestController`, `@RequestMapping("/api/<resource>")`, `@RequiredArgsConstructor`.
- Some are package-private (no `public` modifier) — prefer this for new controllers inside `order/`.
- User-facing modules (`user`, `invoice`, `notification`) tend to use `public class`.
- Always add `@Tag(name, description)` for OpenAPI grouping.

### Services
- Return `GenericResponse.success(data)` or `GenericResponse.success(data, message)` for 200 responses.
- Use `GenericResponse.created(data)` for 201 created responses.
- Throw custom exceptions from `common/exception/` or `<module>/domain/exception/`; the global exception handler maps them to HTTP responses.
- Inject via constructor (`@RequiredArgsConstructor` + `private final`).

### Mappers (MapStruct)
```java
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface FooMapper { ... }
```
Use `@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)` for partial updates.

### DTOs
- Request DTOs use `@Valid` + Bean Validation annotations.
- Response DTOs are plain records or Lombok `@Data` classes in `application/dto/`.

### Exceptions
- `BusinessException` — base for domain rule violations.
- Specific exceptions extend it: `OrderNotFoundException`, `TruckNotFoundException`, etc.
- Always prefer throwing a specific exception over a generic one.

## Database

- MySQL with Flyway migrations (`src/main/resources/db/migration/`).
- Name new migrations `V{N}__{description}.sql` (sequential integer, double underscore).
- Configuration in `application.yml`; secrets loaded via Spring dotenv from `.env`.

## Security

- Spring Security + JWT (jjwt 0.11.5).
- Token stored in `Authorization: Bearer <token>` header.
- Auth flows in `user/` module: `AuthController`, `AuthService`.

## File Storage

- AWS SDK v2 targeting Cloudflare R2 (S3-compatible).
- Config: `CloudflareR2Properties`, `StorageProperties`.
- Upload via `postFormData` pattern through `storage/` outbound adapter.

## Notable Dependencies

| Library | Purpose |
|---|---|
| MapStruct 1.6.3 | DTO ↔ Entity mapping |
| Lombok | Boilerplate reduction |
| Springdoc OpenAPI 2.3.0 | Swagger UI at `/swagger-ui.html` |
| Apache POI 5.5.1 | Excel report generation |
| Spring dotenv 4.0.0 | `.env` file loading |
| 2D-Bin-Packing (local) | Custom pallet packing algorithm |

## Package Root

`com.tupack.palletsortingapi`
