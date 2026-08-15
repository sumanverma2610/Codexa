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