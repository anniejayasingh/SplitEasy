# SplitEasy - Expense Sharing Application (Splitwise Clone)

SplitEasy is a backend system that allows multiple users to **share expenses within groups**. 
Each expense added to a group automatically updates balances among its members.  
The app supports **Google OAuth2 login**, **role-based security**, **Swagger API documentation**, and **transactional expense management**.

---

## 🚀 Features
- **User Authentication**: Google OAuth2 login with role-based security (USER, ADMIN).
- **Group Management**: Create, delete, and manage groups.
- **Expense Management**: Add, update, and fetch expenses within groups.
- **Settlement Service**: Calculate balances and settle debts between group members.
- **Global Exception Handling**: Centralized error handling with meaningful HTTP responses.
- **API Documentation**: Swagger/OpenAPI 3.0 support.
- **Transactional Updates**: Balances updated atomically when expenses are added.

---

## 🛠️ Tech Stack
- **Java 8+**
- **Spring Boot 3.x**
- **Spring Security with OAuth2 (Google Login)**
- **Spring Data JPA (Hibernate)**
- **MySQL/PostgreSQL (configurable)**
- **Swagger (OpenAPI v3)**
- **Lombok**
- **Maven/Gradle**

---

## ⚙️ Architecture Overview
1. **User Service**  
   - Handles user registration, authentication, and fetching user details.  

2. **Group Service**  
   - Create/Delete groups, add/remove users to groups, fetch user groups.  

3. **Expense Service**  
   - Add expenses to a group, list expenses, calculate user contributions.  

4. **Settlement Service**  
   - Calculates balances and provides a simplified settlement mechanism.  

5. **Global Exception Handler**  
   - Uses `@ControllerAdvice` and `@ExceptionHandler` for handling application-wide exceptions.  
   - Returns standardized error responses with appropriate HTTP status codes.  

---

## 🔑 Authentication & Authorization
- Users log in via **Google OAuth2**.
- Roles supported:
  - **USER**: Can create groups, add expenses, and view balances.
  - **ADMIN**: Can delete groups, manage users, and view all expenses.

---

## 📖 API Documentation (Swagger)
Swagger UI is available after running the app:

```
http://localhost:8080/swagger-ui.html
```

### Example: Group Controller Endpoints
| Method | Endpoint                     | Description                    |
|--------|------------------------------|--------------------------------|
| POST   | `/groups`                   | Create a new group             |
| GET    | `/groups`                   | Get all groups                 |
| GET    | `/groups/{id}`              | Get group by ID                |
| POST   | `/groups/{groupId}/addUser/{userId}` | Add user to group |
| DELETE | `/groups/{groupId}/removeUser/{userId}` | Remove user from group |
| DELETE | `/groups/{groupId}`         | Delete a group                 |
| GET    | `/groups/user/{userId}`     | Get all groups for a user      |

---

## 🛡️ Global Exception Handling
- Centralized error handling is implemented with `@RestControllerAdvice`.  
- Common errors handled:
  - `ResourceNotFoundException` → HTTP `404 NOT FOUND`
  - `InvalidRequestException` → HTTP `400 BAD REQUEST`
  - `AccessDeniedException` → HTTP `403 FORBIDDEN`
  - Generic fallback → HTTP `500 INTERNAL SERVER ERROR`

Example response:
```json
{
  "timestamp": "2025-10-18T12:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Group not found",
  "path": "/groups/999"
}
```

---

## 🏃 How to Run the App
1. Clone the repo:
   ```bash
   git clone https://github.com/your-username/spliteasy.git
   cd spliteasy
   ```
2. Configure database in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/spliteasy
   spring.datasource.username=root
   spring.datasource.password=yourpassword
   spring.jpa.hibernate.ddl-auto=update
   ```
3. Configure Google OAuth2 credentials in `application.properties`:
   ```properties
   spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
   spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
   ```
4. Build and run:
   ```bash
   mvn spring-boot:run
   ```
5. Access:
   - API: `http://localhost:8080/api/...`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 🤝 Contribution
1. Fork the repo.  
2. Create a feature branch (`feature/xyz`).  
3. Commit your changes.  
4. Submit a Pull Request.

---

## 📜 License
This project is licensed under the MIT License.
