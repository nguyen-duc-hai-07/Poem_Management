# oplearn-base

Base Spring Boot project dùng làm khung chuẩn cho các service mới: REST API + PostgreSQL + Liquibase + JWT security, kèm sẵn exception handling đa ngôn ngữ, auditing, soft delete và Docker.

## Tech stack

| Thành phần      | Phiên bản |
|-----------------|-----------|
| Java            | 17        |
| Spring Boot     | 3.2.x     |
| PostgreSQL      | 16        |
| Redis           | 7         |
| Liquibase       | managed by Boot |
| springdoc (Swagger UI) | 2.3.0 |
| JJWT            | 0.12.x    |

## Có sẵn những gì

- **CRUD mẫu** (`User`) đủ các tầng: controller → service → repository → entity, dùng làm khuôn khi thêm resource mới.
- **`BaseEntity`**: id số nguyên tự tăng (`BIGSERIAL`), audit (`created_by/at`, `updated_by/at` — tự điền qua `AuditorAware`) và soft delete (`is_deleted`).
- **Response chuẩn** `ResponseGeneral<T>` (snake_case) + `PageResponse<T>` cho danh sách có tổng số bản ghi.
- **Exception handling tập trung** (`ExceptionHandlerAdvice`): ném `NotFoundException` / `ConflictException` / `BadRequestException`, message tự resolve theo `Accept-Language` từ `i18n/messages*.properties` (en, vi).
- **Security**: JWT filter (Bearer token, HS256), route whitelist cho Swagger/actuator, `/api/v1/admin/**` yêu cầu role `ADMIN`, 401/403 trả JSON cùng format response chung. Password mã hoá BCrypt.
- **Auth**: `POST /api/v1/auth/login` (username + password → access token + refresh token), `POST /api/v1/auth/refresh` (xoay vòng refresh token — token cũ bị thu hồi), `POST /api/v1/auth/logout` (cần access token; thu hồi refresh token và blacklist access token — mất hiệu lực ngay). Cả hai token đều là **JWT** cùng secret, phân biệt bằng claim `type` (`access` / `refresh`): access token sống ngắn (`JWT_EXPIRATION_MS`, mặc định 1 giờ, mang claim `roles`), refresh token sống dài (`JWT_REFRESH_EXPIRATION_MS`, mặc định 7 ngày). Trạng thái thu hồi quản lý trên **Redis** theo `jti`: refresh token hợp lệ phải còn key `auth:refresh_token:{jti}`; access token bị logout nằm trong `auth:access_token_blacklist:{jti}` với TTL bằng thời gian còn lại.
- **Mọi API yêu cầu access token** (`Authorization: Bearer ...`), trừ `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh` và whitelist Swagger/actuator. Migration seed sẵn tài khoản `admin` / `admin123` (role `ADMIN`) để đăng nhập lần đầu — **đổi mật khẩu này ở môi trường thật**.
- **Validation**: Bean Validation trên request DTO, message key i18n.
- **Liquibase**: migration trong `src/main/resources/db/changelog/`, khai báo trong `db/master.xml`.
- **Test**: unit test service (Mockito) + context test chạy trên H2 (PostgreSQL mode), không cần Docker.
- **CI**: GitHub Actions chạy `mvn verify` cho mọi push/PR.

## Chạy local

Yêu cầu: JDK 17, Maven 3.8+, PostgreSQL (hoặc Docker).

```bash
# Cách 1: Docker (app + postgres)
cp .env.example .env
docker compose up -d --build

# Cách 2: chạy trực tiếp, Postgres có sẵn ở localhost:5432
mvn spring-boot:run
```

- API: http://localhost:8088/api/v1/users
- Swagger UI: http://localhost:8088/swagger-ui.html
- Health: http://localhost:8088/actuator/health

Cấu hình qua biến môi trường (xem `.env.example`): `POSTGRES_URL`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, `PORT`.

## Test

```bash
mvn verify
```

## Dùng làm base cho service mới

1. Clone repo, đổi `artifactId`/`name` trong `pom.xml` và `spring.application.name` trong `application.yml`.
2. Đổi package `org.oplearn.project` nếu cần (IDE refactor).
3. Thêm resource mới theo khuôn `User`: entity kế thừa `BaseEntity` → repository → service (interface + impl) → controller; thêm changelog Liquibase mới vào `db/changelog/` và include trong `master.xml` (không sửa changelog đã chạy).
4. Sinh `JWT_SECRET` riêng cho từng môi trường: `openssl rand -base64 48`.
5. Thu hẹp `CORS_ALLOWED_ORIGINS` và các rule trong `SecurityConfiguration` theo nhu cầu thực tế (mặc định `/api/v1/**` đang `permitAll` để demo).

## Cấu trúc thư mục

```
src/main/java/org/oplearn/project/
├── annotation/        # @TrackTime (AOP đo thời gian)
├── configuration/     # MessageSource, Async, AOP, JPA Auditing
├── constants/
├── controller/
│   └── advice/        # ExceptionHandlerAdvice
├── dto/request|response/
├── entity/            # BaseEntity + entity nghiệp vụ
├── exception/         # exception nghiệp vụ (base/ chứa khung chung)
├── repository/
├── security/          # SecurityConfiguration, jwt/, error/
├── service/           # interface + impl/
└── utils/
```
