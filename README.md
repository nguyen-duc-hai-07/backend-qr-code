# QR Code Ordering & Delivery Backend System 🚀

Hệ thống Backend cho giải pháp **Gọi món tại bàn qua mã QR Code & Đặt món giao hàng (Delivery)** sử dụng **Spring Boot 3 + Java 17/21 + PostgreSQL + Liquibase + Redis + WebSocket + RustFS (S3-compatible) + JWT Security**. 

Được thiết kế theo kiến trúc chuẩn mực **Facade Pattern** (tương tự dự án `backend-e-commerce`), phân tách rành mạch 3 đối tượng người dùng:
1. **Khách vãng lai (Guest):** Quét mã QR tại bàn, gọi món tại quán (DINE_IN) không cần đăng nhập, theo dõi đơn hàng bằng mã đơn.
2. **Khách hàng (User):** Đăng ký/đăng nhập tài khoản để đặt giao hàng (DELIVERY) về tận nơi, xem lịch sử đơn hàng cá nhân.
3. **Quản trị viên (Admin):** Quản lý danh mục, món ăn, quản lý bàn & xuất file in ảnh thẻ QR chuẩn nét kèm tên bàn, cập nhật trạng thái đơn hàng, điều chỉnh món khi khách gọi nhầm.

---

## 🛠 Tech Stack & Kiến Trúc Hạ Tầng

| Thành phần | Phiên bản / Công nghệ | Vai trò & Mục đích |
|---|---|---|
| **Java** | 17 / 21 | Ngôn ngữ phát triển chính |
| **Spring Boot** | 3.2.2 | Web MVC, Security, Data JPA, Validation, WebSocket, Actuator |
| **Database** | PostgreSQL 16 | Cơ sở dữ liệu quan hệ chính (`qr_code`) |
| **Migration** | Liquibase | Quản lý phiên bản Database schema tự động & an toàn |
| **Realtime** | Spring WebSocket | Giao tiếp 2 chiều thời gian thực (Real-time push đơn hàng cho bếp/thu ngân) |
| **Cache & Session** | Redis 7 + Spring Cache | Quản lý Blacklist Token, Refresh Token, OTP TTL |
| **QR Code Engine** | ZXing 3.5.3 + Java2D | Sinh ảnh thẻ QR code chuẩn nét kèm tên bàn in đậm phục vụ in ấn |
| **Object Storage** | RustFS / AWS S3 API | Lưu trữ file, hình ảnh món ăn (`qrcode-media`) |
| **Security** | JJWT 0.12.5 + Spring Security | Xác thực JWT Access/Refresh Token (HttpOnly Cookie), RBAC |
| **Documentation** | SpringDoc OpenAPI 2.3.0 | Swagger UI tích hợp sẵn nút Authorize JWT |

---

## 📂 Cấu trúc thư mục

```
src/main/java/org/oplearn/project/
├── annotation/         # @TrackTime (AOP Logging thời gian)
├── configuration/      # Config: S3/MinIO, Async, AOP, JPA Auditing, i18n, Swagger, Cache, WebSocket
├── constants/          # Hằng số hệ thống (Auth, Common, Paging, Cache)
├── controller/         # REST Controllers (StoreTable, Menu, Order, Auth, User, File)
│   └── advice/         # ExceptionHandlerAdvice (Xử lý ngoại lệ toàn cục)
├── dto/
│   ├── request/        # Request payload DTOs (có validate @Valid, snake_case)
│   └── response/       # Response DTOs (ResponseGeneral, PageResponse, TokenResponse...)
├── entity/             # JPA Entities (User, StoreTable, Category, Product, Order, OrderItem, BaseEntity)
├── enums/              # UserRole, OrderType, OrderStatus
├── exception/          # Custom Exceptions (base/ và business exceptions)
├── facade/             # Tầng Facade điều phối các Domain Service (TableFacade, MenuFacade, OrderFacade)
├── repository/         # Spring Data JPA & Redis Repositories
├── security/           # JWT Provider, Auth Filter, SecurityConfig, SecurityHelper
├── service/            # Domain Services (Chỉ giao tiếp qua Request / Response DTOs)
├── utils/              # Tiện ích bổ trợ (ClientIpUtils, DateUtils...)
└── websocket/          # EchoWebSocketHandler, WebSocketAuthInterceptor
```

---

## 🚀 Hướng dẫn khởi chạy dự án

### 1. Khởi động PostgreSQL & Redis qua Docker Compose:
```bash
docker compose up -d postgres redis
```

### 2. Chạy ứng dụng Spring Boot:
```bash
mvn spring-boot:run
```

---

## 🔗 Các đường dẫn truy cập quan trọng

- **Swagger UI (OpenAPI):** [http://localhost:8089/swagger-ui.html](http://localhost:8089/swagger-ui.html)
- **Health Check:** [http://localhost:8089/actuator/health](http://localhost:8089/actuator/health)
- **WebSocket Endpoint:** `ws://localhost:8089/ws?token=<access_token>`
