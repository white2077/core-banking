# 🏦 Core Bank Demo

> Hệ thống Core Banking với kiến trúc **Command Dispatcher Pattern** - Thiết kế để dễ mở rộng, bảo trì và sẵn sàng cho Microservice.

---

## 📋 Mục Lục

1. [Giới Thiệu](#1-giới-thiệu)
2. [Kiến Trúc Hệ Thống](#2-kiến-trúc-hệ-thống)
3. [Cấu Trúc Project](#3-cấu-trúc-project)
4. [Hướng Dẫn Khởi Chạy](#4-hướng-dẫn-khởi-chạy)
5. [Khả Năng Mở Rộng](#5-khả-năng-mở-rộng)
6. [Authentication & Authorization](#6-authentication--authorization)
7. [Code Quality & Standards](#7-code-quality--standards)
8. [Đánh Giá Tổng Quan](#8-đánh-giá-tổng-quan)

---

## 1. Giới Thiệu

### 1.1 Mô Tả Project

**Core Bank Demo** là một hệ thống ngân hàng lõi được xây dựng với Spring Boot, áp dụng kiến trúc **Command Dispatcher Pattern**. Project được thiết kế với mục tiêu:

- ✅ **Single Entry Point** - Một endpoint xử lý tất cả nghiệp vụ
- ✅ **Annotation-driven** - Khai báo nghiệp vụ qua annotations
- ✅ **Auto Audit** - Tự động ghi log mọi thay đổi dữ liệu
- ✅ **Microservice Ready** - Sẵn sàng tách thành microservices

### 1.2 Tech Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Spring Boot | 4.0.2 |
| Language | Java | 17 |
| Database | H2 (dev) / PostgreSQL (prod) | - |
| Security | Spring Security + OAuth2 Resource Server | - |
| Logging | Log4j2 | - |
| Build | Maven | 3.8+ |
| Code Quality | Spotless + PMD | - |

---

## 2. Kiến Trúc Hệ Thống

### 2.1 Command Dispatcher Pattern

```
┌─────────────────────────────────────────────────────────────────────┐
│                           CLIENT                                     │
│                      (Mobile/Web/API)                                │
└─────────────────────────────┬───────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        FILTER CHAIN                                  │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐   │
│  │RequestHeaderFilter│→│  LoggingFilter   │→│  SecurityFilter  │   │
│  │  (Context Setup)  │  │(Log + Mask Token)│  │(JWT Validation) │   │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘   │
└─────────────────────────────┬───────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      CONTROLLER LAYER                                │
│  ┌─────────────────────────────────────────────────────────────────┐│
│  │ CoreController                                                   ││
│  │ POST /core/execute      → Tất cả nghiệp vụ                      ││
│  │ POST /core/authenticate → Xác thực                               ││
│  └─────────────────────────────────────────────────────────────────┘│
└─────────────────────────────┬───────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      DISPATCHER LAYER                                │
│  ┌─────────────────────────────────────────────────────────────────┐│
│  │ FunctionDispatcher                                               ││
│  │ → Lookup handler từ FunctionRegistry                             ││
│  │ → Execute handler                                                ││
│  └─────────────────────────────────────────────────────────────────┘│
│                              │                                       │
│                              ▼                                       │
│  ┌─────────────────────────────────────────────────────────────────┐│
│  │ FunctionRegistry (Khởi tạo lúc startup)                          ││
│  │ → Scan tất cả @Function classes                                  ││
│  │ → Đăng ký @Operation methods                                     ││
│  │ → Build handler map: "CUSTOMER_RETAIL.CREATE" → handler          ││
│  └─────────────────────────────────────────────────────────────────┘│
└─────────────────────────────┬───────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      FUNCTION LAYER                                  │
│                                                                      │
│  ┌─────────────────────┐     ┌─────────────────────┐                │
│  │@Function("CUSTOMER_ │     │    AOP ASPECTS      │                │
│  │  RETAIL")           │     │ ┌─────────────────┐ │                │
│  │                     │ ←── │ │ @Transactional  │ │                │
│  │ @Operation("CREATE")│     │ │ @Auditable      │ │                │
│  │ @Operation("UPDATE")│     │ │ @PreAuthorize   │ │                │
│  │ @Operation("DELETE")│     │ └─────────────────┘ │                │
│  │ @Operation("GET")   │     └─────────────────────┘                │
│  └──────────┬──────────┘                                            │
└─────────────┼───────────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────────────┐
│                      REPOSITORY LAYER                                │
│  ┌─────────────────────┐  ┌─────────────────────┐                   │
│  │CustomerRetailRepo   │  │  AuditRepository    │                   │
│  │extends JpaRepository│  │                     │                   │
│  └──────────┬──────────┘  └──────────┬──────────┘                   │
└─────────────┼────────────────────────┼──────────────────────────────┘
              │                        │
              ▼                        ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         DATABASE                                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐               │
│  │customer_retail│  │ audit_record │  │    users    │               │
│  └──────────────┘  └──────────────┘  └──────────────┘               │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.2 Luồng Xử Lý Request

**Ví dụ: Tạo khách hàng mới**

```
1. Client gửi request:
   POST /core/execute
   {
     "destination": "CUSTOMER_RETAIL",
     "action": "CREATE",
     "data": { "firstName": "Nguyen", "lastName": "Van A", ... }
   }

2. RequestHeaderFilter:
   → Trích xuất clientMessageId
   → Set RequestContext

3. LoggingFilter:
   → Log request body (mask sensitive fields)

4. SecurityFilter:
   → Validate JWT token
   → Set Authentication

5. CoreController.execute():
   → Validate request (@Valid)
   → Gọi dispatcher.dispatch(request)

6. FunctionDispatcher:
   → Lookup handler: "CUSTOMER_RETAIL.CREATE"
   → Gọi handler.execute(request)

7. FunctionRegistry.invokeViaProxy():
   → Gọi method qua Spring Proxy (AOP hoạt động)

8. [AOP] @PreAuthorize:
   → Kiểm tra user có quyền TELLER

9. [AOP] @Transactional:
   → Bắt đầu transaction

10. [AOP] @Auditable:
    → Chuẩn bị capture old/new values

11. CustomerRetailFunction.create():
    → Validate DTO
    → Map sang Entity
    → Lưu vào database
    → Return Response.ok(entity)

12. [AOP] @Auditable:
    → Lưu AuditRecord với newValue

13. [AOP] @Transactional:
    → Commit transaction

14. LoggingFilter:
    → Log response (mask sensitive fields)
    → Log duration

15. Response trả về Client:
    {
      "code": "00",
      "message": "Success",
      "data": { "id": "uuid-xxx", ... }
    }
```

---

## 3. Cấu Trúc Project

```
src/main/java/com/core/bank/demo/
│
├── 📂 channel/rest/              # API Layer
│   └── CoreController.java       # Single entry point
│
├── 📂 dispatcher/                # Routing Layer
│   └── FunctionDispatcher.java   # Route request đến handler
│
├── 📂 config/                    # Configuration
│   ├── function/registry/        # Operation registration
│   │   └── FunctionRegistry.java
│   ├── filter/                   # HTTP Filters
│   │   ├── header/               # Request context
│   │   └── log/                  # Logging với mask sensitive
│   ├── exception/                # Error handling
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   ├── SystemException.java
│   │   └── ErrorCode.java
│   └── security/                 # JWT Security
│
├── 📂 contract/                  # API Contracts
│   ├── Request.java              # Standard request format
│   ├── Response.java             # Standard response format
│   ├── @Function                 # Đánh dấu business function
│   ├── @Operation                # Đánh dấu operation method
│   ├── @Auditable                # Đánh dấu cần audit
│   └── OperationHandler.java     # Functional interface
│
├── 📂 function/                  # Business Logic Layer
│   ├── CustomerRetailFunction.java
│   └── LoginFunction.java
│
├── 📂 entity/                    # JPA Entities
│   ├── BaseEntity.java           # Common audit fields
│   ├── CustomerRetail.java
│   └── AuditRecord.java
│
├── 📂 dto/                       # Data Transfer Objects
│   ├── CustomerCreateRetailDto.java
│   └── CustomerUpdateRetailDto.java
│
├── 📂 repository/                # Data Access Layer
│   ├── CustomerRetailRepository.java
│   └── AuditRepository.java
│
├── 📂 audit/                     # Cross-cutting Concerns
│   └── AuditAspect.java          # AOP audit logging
│
├── 📂 enums/                     # Enumerations
│   ├── CustomerStatus.java
│   └── Role.java
│
├── 📂 util/                      # Utilities
│   ├── DataMapperUtil.java       # Object mapping + validation
│   └── JsonUtils.java            # JSON + sensitive masking
│
└── DemoApplication.java          # Entry point
```

---

## 4. Hướng Dẫn Khởi Chạy

### 4.1 Yêu Cầu Hệ Thống

- Java 17+
- Maven 3.8+
- Git

### 4.2 Cài Đặt & Chạy

```bash
# Clone repository
git clone <repo-url>
cd demo

# Cài đặt dependencies và build
./mvnw clean install -DskipTests

# Chạy application
./mvnw spring-boot:run

# Hoặc chạy file JAR
java -jar target/demo-1.0.0.jar
```

### 4.3 Kiểm Tra Hoạt Động

```bash
# 1. Login để lấy token
curl -X POST http://localhost:8080/core/authenticate \
  -H "Authorization: Basic YWRtaW46MQ==" \
  -H "Content-Type: application/json" \
  -d '{
    "destination": "AUTH",
    "action": "LOGIN",
    "data": {}
  }'

# 2. Tạo khách hàng (thay <token> bằng accessToken từ bước 1)
curl -X POST http://localhost:8080/core/execute \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "destination": "CUSTOMER_RETAIL",
    "action": "CREATE",
    "data": {
      "firstName": "Nguyen",
      "lastName": "Van A",
      "email": "nguyenvana@gmail.com",
      "phoneNumber": "0901234567"
    }
  }'
```

### 4.4 H2 Console (Development)

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: *(để trống)*

---

## 5. Khả Năng Mở Rộng

### 5.1 Thêm Nghiệp Vụ Mới (Cực Kỳ Nhanh)

Chỉ cần **3 bước** để thêm nghiệp vụ mới:

```java
// Bước 1: Tạo Function class
@Function("ACCOUNT")
@RequiredArgsConstructor
public class AccountFunction {

    private final AccountRepository accountRepository;

    // Bước 2: Thêm @Operation methods
    @Operation("OPEN")
    @Transactional
    @Auditable(action = "OPEN", objectType = "ACCOUNT")
    @PreAuthorize("hasAuthority('TELLER')")
    public Response openAccount(Request req) {
        // Bước 3: Viết business logic
        // ... code nghiệp vụ
        return Response.ok(account);
    }

    @Operation("CLOSE")
    @Transactional
    @Auditable(action = "CLOSE", objectType = "ACCOUNT", entityClass = Account.class)
    @PreAuthorize("hasAuthority('ADMIN')")
    public Response closeAccount(Request req) {
        // ... code nghiệp vụ
        return Response.ok(result);
    }
}
```

**Không cần:**
- ❌ Sửa Controller
- ❌ Sửa Dispatcher
- ❌ Sửa Registry
- ❌ Cấu hình routing

### 5.2 Chuyển Sang Microservice

Kiến trúc hiện tại **sẵn sàng cho Microservice**. Chỉ cần thêm **API Gateway**:

```
┌─────────────────────────────────────────────────────────────────┐
│                        API GATEWAY                               │
│                    POST /core/execute                            │
│                                                                  │
│  Route theo "destination":                                       │
│  ├── CUSTOMER_RETAIL  → customer-service:8081                   │
│  ├── ACCOUNT          → account-service:8082                    │
│  └── TRANSACTION      → transaction-service:8083                │
└─────────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌───────────────┐     ┌───────────────┐     ┌───────────────┐
│customer-service│     │account-service│     │transaction-svc│
│               │     │               │     │               │
│ GIỮ NGUYÊN:   │     │ GIỮ NGUYÊN:   │     │ GIỮ NGUYÊN:   │
│ -FunctionReg  │     │ -FunctionReg  │     │ -FunctionReg  │
│ -Dispatcher   │     │ -Dispatcher   │     │ -Dispatcher   │
│ -@Function    │     │ -@Function    │     │ -@Function    │
│ -@Operation   │     │ -@Operation   │     │ -@Operation   │
└───────────────┘     └───────────────┘     └───────────────┘
```

**Lợi ích:**
- ✅ Mỗi service giữ nguyên kiến trúc
- ✅ Chỉ cần tách module, không refactor
- ✅ Shared library cho common code

### 5.3 Tính Năng Tự Động

| Tính Năng | Cách Kích Hoạt | Mô Tả |
|-----------|---------------|-------|
| **Auto Audit** | `@Auditable` | Tự động ghi log old/new values |
| **Auto Transaction** | `@Transactional` | Tự động commit/rollback |
| **Auto Authorization** | `@PreAuthorize` | Tự động kiểm tra quyền |
| **Auto Validation** | `@Valid` + DTO annotations | Tự động validate input |
| **Auto Mask Sensitive** | Tự động | Mask token/password trong logs |

---

## 6. Authentication & Authorization

### 6.1 Trạng Thái Hiện Tại

> ⚠️ **Lưu ý**: Module Auth hiện tại chỉ để **test/demo**. Sẽ được thay thế bằng **Keycloak + Redis** trong production.

**Hiện tại:**
- Mock user được tạo lúc startup (`admin/1`)
- JWT token tự generate
- H2 database lưu user

**Đã chuẩn bị sẵn:**
- ✅ `spring-boot-starter-security-oauth2-resource-server`
- ✅ JWT validation infrastructure
- ✅ Role-based authorization (`@PreAuthorize`)

### 6.2 Kế Hoạch Tích Hợp

```
┌─────────────────────────────────────────────────────────────────┐
│                        PRODUCTION SETUP                          │
│                                                                  │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐         │
│  │  Keycloak   │───▶│    Redis    │◀───│ Core Bank   │         │
│  │ (Identity)  │    │(Token Cache)│    │  Service    │         │
│  └─────────────┘    └─────────────┘    └─────────────┘         │
│                                                                  │
│  Flow:                                                           │
│  1. User login qua Keycloak                                      │
│  2. Keycloak trả JWT token                                       │
│  3. Token được cache trong Redis                                 │
│  4. Core Bank validate token từ Redis/Keycloak                   │
└─────────────────────────────────────────────────────────────────┘
```

### 6.3 Roles & Permissions

| Role | Quyền |
|------|-------|
| `TELLER` | CREATE, UPDATE, GET customer |
| `ADMIN` | DELETE customer |
| `APPROVER` | Approve transactions |

---

## 7. Code Quality & Standards

### 7.1 Công Cụ Kiểm Tra

| Tool | Mục Đích | Lệnh                                  |
|------|----------|---------------------------------------|
| **Spotless** | Format code, xóa unused imports | `mvn spotless:apply`                  |
| **PMD** | Phát hiện bad practices | `mvn pmd:check`                       |
| **CPD** | Phát hiện code trùng lặp | `mvn pmd:cpd-check`                   |
| **Git Hooks** | Tự động check trước commit | Tự động (Phải chạy mvn compile trước) |

### 7.2 PMD Rules Chi Tiết

**PMD** (Bad Practices) - Cấu hình trong `pmd-rules.xml`:

| Category | Rules | Mô Tả |
|----------|-------|-------|
| **Code Style** | EmptyControlStatement, UnnecessaryReturn | Empty blocks, code thừa |
| **Best Practices** | UnusedLocalVariable, MissingOverride | Code không sử dụng |
| **Error Prone** | EmptyCatchBlock, OverrideBothEqualsAndHashcode | Lỗi tiềm ẩn |
| **Design** | NcssCount (>60), ExcessiveParameterList (>6), CyclomaticComplexity (>15) | Code phức tạp |
| **Performance** | StringInstantiation, InefficientStringBuffering | Hiệu năng kém |

**CPD** (Copy-Paste Detector) - Cấu hình trong `pom.xml`:

| Config | Value | Mô Tả |
|--------|-------|-------|
| `minimumTokens` | 60 | Số token tối thiểu để coi là duplicate |
| `ignoreAnnotations` | true | Bỏ qua annotations |
| `ignoreLiterals` | true | Bỏ qua string/number literals |

**Loại trừ (không check):**
- `**/entity/*.java` - Entity classes
- `**/dto/*.java` - DTO classes  
- `**/enums/*.java` - Enum classes
- Annotations (`@Column`, `@Id`, etc.)
- Common literals (`id`, `name`, `value`, `type`, `message`, `code`, `status`)

### 7.3 Chạy Kiểm Tra Code

```bash
# Format code
./mvnw spotless:apply

# Check bad practices
./mvnw pmd:check

# Check code trùng lặp
./mvnw pmd:cpd-check

# Check tất cả (khi build)
./mvnw verify
```

### 7.4 Coding Standards

- **Indent**: 4 spaces
- **Line width**: 120 characters
- **Import order**: java → javax → jakarta → org → com
- **Naming**:
  - Function class: `{Domain}Function` (vd: `CustomerRetailFunction`)
  - DTO: `{Domain}{Action}Dto` (vd: `CustomerCreateRetailDto`)
  - Repository: `{Entity}Repository` (vd: `CustomerRetailRepository`)


---

## 8. Đánh Giá Tổng Quan

### 8.1 Điểm Đánh Giá

| Tiêu Chí | Điểm | Nhận Xét |
|----------|------|----------|
| **Kiến Trúc** | ⭐⭐⭐⭐⭐ 9/10 | Command Dispatcher pattern, scalable |
| **Clean Code** | ⭐⭐⭐⭐ 8/10 | Rõ ràng, dễ đọc |
| **DRY Principle** | ⭐⭐⭐⭐ 8.5/10 | Không duplicate, utilities tốt |
| **Error Handling** | ⭐⭐⭐⭐ 8.5/10 | Centralized, structured error codes |
| **Security** | ⭐⭐⭐⭐ 8/10 | JWT ready, mask sensitive data |
| **Extensibility** | ⭐⭐⭐⭐⭐ 9/10 | Thêm nghiệp vụ cực nhanh |
| **Microservice Ready** | ⭐⭐⭐⭐⭐ 9/10 | Chỉ cần thêm Gateway |
| **Code Quality Tools** | ⭐⭐⭐⭐ 8/10 | Spotless + PMD + Git hooks |
| **Testing** | ⭐⭐ 2/10 | Cần bổ sung |
| **Documentation** | ⭐⭐⭐ 6/10 | README có, API docs chưa |

### 8.2 Điểm Mạnh

| # | Điểm Mạnh |
|---|-----------|
| 1 | Single entry point - dễ bảo mật, monitor |
| 2 | Annotation-driven - code nghiệp vụ nhanh |
| 3 | Auto audit - không cần code manual |
| 4 | Sensitive masking - bảo mật logs |
| 5 | Microservice ready - không cần refactor |
| 6 | Consistent API format - dễ integrate |

---

## 📞 Liên Hệ

- **Author**: Hoang Dung white2077
- **Email**: dungbui8198@gmail.com
- **Version**: 1.0.0
- **Last Updated**: February 2026

---

