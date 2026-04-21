# SplitEasy

## Overview
SplitEasy is a Spring Boot-based expense management application designed to help users efficiently track, manage, and share their expenses. The application aims to simplify the process of splitting expenses among friends, family, or groups, ensuring transparency and ease of use.

## Features
- **User Authentication**: Secure login and user registration.
- **Expense Tracking**: Add, edit, and delete expenses with ease.
- **Group Management**: Create and manage groups to share expenses.
- **Expense Splitting**: Automatically calculate and split expenses among group members.
- **Reporting**: View detailed reports of expenses over time.

## Technologies Used
- **Spring Boot**: Framework for building the application.
- **Thymeleaf**: For rendering HTML views.
- **Spring Security**: For authentication and authorization.
- **JPA/Hibernate**: To interact with the database.
- **MySQL**: Database for storing user and expense data.

## Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/anniejayasingh/SplitEasy.git
   ```
2. Navigate to the project directory:
   ```bash
   cd SplitEasy
   ```
3. Configure application properties in `src/main/resources/application.properties`.
4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

## Usage
- Visit `http://localhost:8080` to access the application.
- Create an account or log in to manage your expenses.

## Contributing
Contributions are welcome! Please submit a pull request or create an issue for any improvements or bug fixes.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgements
- Thank you to all contributors and supporters of this project.