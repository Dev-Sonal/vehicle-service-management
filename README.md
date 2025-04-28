# Vehicle Service Management System

A comprehensive Spring Boot application for managing vehicle service operations, customer information, and service records.

## Features

- User authentication and authorization
- Role-based access control (Admin, Service Advisor, Customer)
- Vehicle management
- Service record tracking
- Customer management
- Invoice generation
- Service item management
- Dashboard for different user roles

## Technologies Used

- Spring Boot
- Spring Security
- Spring Data JPA
- Thymeleaf
- Bootstrap
- MySQL Database
- Maven

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven
- MySQL 8.0 or higher

### Installation

1. Clone the repository
```bash
git clone https://github.com/Dev-Sonal/vehicle-service-management.git
```

2. Configure the database
- Create a MySQL database
- Update `application.properties` with your database credentials

3. Build the project
```bash
mvn clean install
```

4. Run the application
```bash
mvn spring-boot:run
```

## Project Structure

```
src/main/java/com/tch/vehicle/
├── config/         # Configuration classes
├── controller/     # MVC controllers
├── dto/           # Data Transfer Objects
├── entity/        # JPA entities
├── repository/    # JPA repositories
└── service/       # Business logic
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 