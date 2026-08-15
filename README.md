# Codexa - Online Code Judge Backend

Codexa is a backend application for an online coding platform where users can solve programming problems, submit Java code, and receive automatic results based on predefined test cases.

The project is built using Spring Boot and provides REST APIs for authentication, problem management, test cases, code execution, submissions, and role-based authorization.

---

## 🚀 Features

### 🔐 Authentication & Authorization
- User registration and login
- JWT-based authentication
- Role-based authorization
- ADMIN and USER roles
- Protected REST APIs
- Users can access only their own submissions
- Unauthorized access returns `403 Forbidden`

### 🧩 Problem Management
- Create and manage coding problems
- Problem-based test cases
- Admin-only test case management

### 🧪 Test Case Management
- Create test cases for problems
- Input and expected output support
- Sample test cases
- Hidden test cases
- Test cases stored in MySQL

### ⚙️ Java Code Execution
- Compile submitted Java code using `javac`
- Execute compiled Java programs
- Pass test-case input through `System.in`
- Capture program output
- Detect compilation errors
- Detect runtime errors
- 5-second execution timeout

### 🧑‍⚖️ Code Judge
Submitted code is executed against the problem's test cases.

The submission can receive:

- `PENDING`
- `RUNNING`
- `ACCEPTED`
- `WRONG_ANSWER`
- `COMPILATION_ERROR`
- `RUNTIME_ERROR`
- `TIME_LIMIT_EXCEEDED`

### 📊 Submission Management
- Submit code
- View user's submissions
- View individual submissions
- Submission ownership validation
- Store submission code, language, result and status

### 🛡️ Error Handling
- Global exception handling
- Validation error responses
- Duplicate email handling
- Resource not found handling
- Forbidden access handling
- Consistent HTTP status codes

---

## 🛠️ Technologies Used

### Backend
- Java
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- Hibernate
- JWT


### Database
- MySQL

### Tools
- IntelliJ IDEA
- Postman
- Git
- GitHub
- Maven
