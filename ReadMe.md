# Contact Management System

A full-stack web application for managing personal contacts, built as part of a Java internship project at 10p Shine.

## Tech Stack

**Backend**
- Java 17
- Spring Boot 3.5
- Spring Security + JWT Authentication
- Spring Data JPA + Hibernate
- Microsoft SQL Server
- SLF4J + Logback
- JUnit 5 + Mockito
- SonarCloud (code quality)

**Frontend**
- React.js
- Axios
- React Router

## Features

### User Authentication
- Register using email or phone number
- Login with email or phone number
- Change password at any time
- JWT-based stateless authentication

### Contact Management
- View all contacts in a paginated list
- Search and filter contacts by name
- Create, update, and delete contacts
- Each contact supports:
    - First name, last name, title
    - Multiple email addresses with labels (work, personal, other)
    - Multiple phone numbers with labels (mobile, work, home, personal, other)

### Export / Import
- Export all contacts to a CSV file
- Import contacts from a CSV file

### Application Logging
- SLF4J with Logback throughout the application
- Logs important events, errors, and user activities

### Exception Handling
- Global exception handler for meaningful error messages
- Validation on all inputs

## Project Structure
    Contact-Management-system/
    ├── BackEnd/                  # Spring Boot backend
    │   ├── src/main/java/com/cms/backend/
    │   │   ├── controller/       # REST controllers
    │   │   ├── service/          # Business logic
    │   │   ├── repository/       # JPA repositories
    │   │   ├── entity/           # Database entities
    │   │   ├── dto/              # Data transfer objects
    │   │   ├── security/         # JWT + Spring Security
    │   │   └── exception/        # Global exception handling
    │   ├── src/test/             # JUnit + Mockito tests
    │   └── Database/             # SQL schema scripts
    └── FrontEnd/
    └── cms-frontend/         # React.js frontend
    └── src/
    ├── pages/        # Login, Register, Contacts, Profile
    └── services/     # Axios API service
## Running Locally

### Prerequisites
- Java 17
- Node.js 18+
- Microsoft SQL Server (SQLEXPRESS)
- Maven 3.9+

### Database Setup
1. Open SSMS and connect to your SQL Server instance
2. Create a database: `CREATE DATABASE ContactManagementDB;`
3. Create a SQL login: `CREATE LOGIN cms_user WITH PASSWORD = 'YourPassword';`
4. Run the schema script from `BackEnd/Database/schema.sql`

### Backend
1. Update `BackEnd/src/main/resources/application.properties` with your DB credentials
2. Open the `BackEnd` folder in IntelliJ IDEA
3. Run `BackendApplication.java`
4. Backend runs on `http://localhost:8080`

### Frontend
```bash
cd FrontEnd/cms-frontend
npm install
npm start
```
Frontend runs on `http://localhost:3000`

## API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | /api/auth/register | Register new user | No |
| POST | /api/auth/login | Login | No |
| POST | /api/auth/change-password | Change password | Yes |
| GET | /api/contacts | Get all contacts (paginated) | Yes |
| POST | /api/contacts | Create contact | Yes |
| PUT | /api/contacts/{id} | Update contact | Yes |
| DELETE | /api/contacts/{id} | Delete contact | Yes |
| GET | /api/contacts/export | Export contacts to CSV | Yes |
| POST | /api/contacts/import | Import contacts from CSV | Yes |
| GET | /api/user/profile | Get user profile | Yes |

## Testing
```bash
cd BackEnd
mvn test
```
14 unit tests covering AuthService and ContactService.

## Code Quality
Analyzed with SonarCloud. View the report:
https://sonarcloud.io/project/overview?id=Ibrahim5570_Contact-Management-system