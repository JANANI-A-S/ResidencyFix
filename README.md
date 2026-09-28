# ResidencyFix

ResidencyFix is a professional hostel maintenance complaint tracker built using Spring Boot, Maven, MySQL, Spring Web, Spring Data JPA, Validation, and Thymeleaf.

## Features
- Add, edit, delete, and view complaints
- Filter by complaint status
- Search by resident name
- Validation for required fields
- MySQL integration using Spring Data JPA
- Professional UI built with Thymeleaf and custom CSS

## Tech Stack
- Java 21
- Spring Boot 3.3.x
- Maven
- MySQL Workbench / MySQL Server
- Spring Web
- Spring Data JPA
- Bean Validation
- Thymeleaf
- Postman for API testing

## Prerequisites
- Java 21+
- Maven 3.9+
- MySQL Server installed and running
- MySQL Workbench optional but recommended

## Database Setup
1. Open MySQL Workbench.
2. Create a database named `residencyfix`.
3. Update credentials in `src/main/resources/application.properties` if needed.

Example:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/residencyfix?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

## Run the application
```bash
mvn spring-boot:run
```

Then open:
- http://localhost:8080/

## API Endpoints
- `GET /complaints`
- `GET /complaints/{id}`
- `POST /complaints`
- `POST /complaints/{id}/update`
- `POST /complaints/{id}/delete`

## Postman
Use Postman to test the complaint endpoints by sending JSON payloads such as:

```json
{
  "residentName": "Aarav Sharma",
  "roomNumber": "B-204",
  "category": "Plumbing",
  "description": "Water leakage from bathroom pipe.",
  "status": "NEW"
}
```

## Notes
This project matches the problem statement for a hostel complaint tracker and is structured for professional academic submission.
