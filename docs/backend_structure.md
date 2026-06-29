backend/
├── pom.xml                        # Nơi khai báo các thư viện (dependencies) và cấu hình build của Maven
├── src/
│   ├── main/
│   │   ├── java/org/eduspace/backend/
│   │   │   ├── BackendApplication.java # File gốc chứa hàm main để chạy ứng dụng Spring Boot
│   │   │   │
│   │   │   ├── config/            # Chứa các file cấu hình ứng dụng
│   │   │   ├── controller/        # Tầng API: Nơi tiếp nhận các HTTP request từ client và trả về HTTP response
│   │   │   ├── dto/               # Data Transfer Objects: Các đối tượng dùng để đóng gói và truyền tải dữ liệu giữa Client - Server
│   │   │   ├── entity/            # Chứa các Model class ánh xạ trực tiếp với các bảng trong cơ sở dữ liệu MySQL (Dùng JPA/Hibernate)
│   │   │   ├── exception/         # Chứa các class xử lý lỗi (Exception) tùy chỉnh và Global Exception Handler để chuẩn hóa format lỗi trả về cho frontend
│   │   │   ├── repository/        # Tầng Database: Các interface kế thừa JpaRepository để thao tác trực tiếp với Database
│   │   │   ├── security/          # Tầng Bảo Mật: Chứa các cấu hình Filter, Interceptor xử lý Session, xác thực và phân quyền
│   │   │   └── service/           # Tầng Business Logic: Nơi chứa toàn bộ logic nghiệp vụ cốt lõi của ứng dụng (xử lý dữ liệu trước khi lưu hoặc sau khi lấy từ DB)
│   │   │
│   │   └── resources/
│   │       ├── application.properties # File cấu hình môi trường của Spring Boot (kết nối Database, JWT secret, server port...)
│   │       └── static/            # Chứa các tài nguyên tĩnh như file CSS, JS, Images
│   │       └── templates/         # Chứa các file HTML template
│   │
│   └── test/                      # Thư mục chứa các file kiểm thử tự động (Unit Test, Integration Test)
│       └── java/org/eduspace/backend/
│           └── BackendApplicationTests.java