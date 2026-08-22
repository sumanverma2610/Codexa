# Codexa — Online Code Judge Backend

Codexa is a backend REST API for an online coding platform where users can browse programming problems, submit solutions, and receive automated judging results.

The application is built with **Java, Spring Boot, Spring Security, JWT, JPA/Hibernate, and MySQL**. It includes role-based access control, secure authentication, test-case management, Java code execution, and submission tracking.

---

## 🚀 Key Features

### Authentication & Security
- JWT-based authentication
- Secure user registration and login
- BCrypt password hashing
- Role-based authorization with `ADMIN` and `USER`
- Protected REST APIs
- Ownership validation for user submissions
- Centralized exception handling

### Problem & Test Case Management
- Create and manage coding problems
- Associate multiple test cases with each problem
- Support for sample and hidden test cases
- Admin-controlled test case management

### Code Execution
- Java source-code compilation using `javac`
- Automatic program execution
- Input/output handling
- Compilation error detection
- Runtime error detection
- Execution timeout handling

### Submission & Judging
- Submit programming solutions
- Execute submissions against test cases
- Store submission code and execution results
- Track submission status
- View personal submission history
- Prevent users from accessing other users' submissions

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| Language | Java |
| Framework | Spring Boot |
| Security | Spring Security + JWT |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL |
| API | REST |
| Validation | Jakarta Bean Validation |
| Build Tool | Maven |
| API Testing | Postman |
| Version Control | Git / GitHub |

---

## 🏗️ Architecture

```text
                    ┌─────────────────┐
                    │     Client      │
                    │    Postman /    │
                    │    Frontend     │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │   REST API      │
                    │   Controllers   │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │    Services     │
                    │ Business Logic  │
                    └──────┬─────┬────┘
                           │     │
                ┌──────────┘     └──────────┐
                ▼                           ▼
        ┌───────────────┐           ┌────────────────┐
        │  Repositories │           │ Code Executor  │
        │     JPA       │           │ javac + java   │
        └───────┬───────┘           └───────┬────────┘
                │                           │
                ▼                           ▼
        ┌───────────────┐           ┌────────────────┐
        │     MySQL     │           │ Test Execution │
        └───────────────┘           └────────────────┘


## Author
Suman Verma